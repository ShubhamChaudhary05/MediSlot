/*
 * Real API audit runner. It creates clearly labelled QA accounts in the configured
 * database; it never logs credentials or JWTs. Run with QA_ADMIN_EMAIL,
 * QA_ADMIN_PASSWORD and QA_PASSWORD set in the process environment.
 */
const base = process.env.QA_API_BASE ?? "http://localhost:8080/api";
const runId = process.argv[3] ?? process.argv[2] ?? "audit-local";
const mode = process.argv[2] === "verify" ? "verify" : "full";
const adminEmail = process.env.QA_ADMIN_EMAIL;
const adminPassword = process.env.QA_ADMIN_PASSWORD;
const password = process.env.QA_PASSWORD;

if (!adminEmail || !adminPassword || !password) throw new Error("QA_ADMIN_EMAIL, QA_ADMIN_PASSWORD and QA_PASSWORD must be configured.");

const results = [];
function ok(name, detail) { results.push({ name, ok: true, detail }); }
function fail(name, detail) { results.push({ name, ok: false, detail }); }
function ensure(condition, name, detail) { if (!condition) throw new Error(`${name}: ${detail}`); }

async function call(method, path, body, token) {
  const response = await fetch(`${base}${path}`, {
    method,
    headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    ...(body === undefined ? {} : { body: JSON.stringify(body) }),
  });
  const json = await response.json().catch(() => null);
  return { status: response.status, body: json };
}

async function login(email, pass) {
  const response = await call("POST", "/auth/login", { email, password: pass });
  ensure(response.status === 200, "login", `expected 200, got ${response.status}`);
  return response.body.data.accessToken;
}

async function register(payload) {
  const response = await call("POST", "/auth/register", payload);
  ensure(response.status === 201, "registration", `expected 201, got ${response.status}`);
  return response.body.data;
}

const names = {
  patient: `QA Patient ${runId}`,
  doctorApproved: `QA Doctor Approved ${runId}`,
  doctorRejected: `QA Doctor Rejected ${runId}`,
  hospital: `QA Hospital ${runId}`,
};
const emails = {
  patient: `qa-patient-${runId}@medislot.test`,
  doctorApproved: `qa-doctor-approved-${runId}@medislot.test`,
  doctorRejected: `qa-doctor-rejected-${runId}@medislot.test`,
  hospital: `qa-hospital-${runId}@medislot.test`,
};

async function verifyPersistence() {
  const patientToken = await login(emails.patient, password);
  const appointments = await call("GET", "/appointments", undefined, patientToken);
  ensure(appointments.status === 200, "persistence", `patient appointments returned ${appointments.status}`);
  ensure(appointments.body.data.some((appointment) => appointment.status === "CANCELLED"), "persistence", "cancelled appointment was not persisted");
  const doctorSearch = await call("GET", `/doctors?q=${encodeURIComponent(names.doctorApproved)}`);
  ensure(doctorSearch.status === 200 && doctorSearch.body.data.content.some((doctor) => doctor.name === names.doctorApproved), "persistence", "approved doctor is missing after restart");
  const hospitalSearch = await call("GET", `/hospitals?q=${encodeURIComponent(names.hospital)}`);
  ensure(hospitalSearch.status === 200 && hospitalSearch.body.data.content.some((hospital) => hospital.name === names.hospital), "persistence", "approved hospital is missing after restart");
  ok("persistence after restart", "cancelled appointment and approved providers were read from PostgreSQL");
}

async function run() {
  const health = await call("GET", "/health");
  ensure(health.status === 200 && health.body.status === "UP", "health", "API did not report UP");
  ok("backend health", "API reported UP");

  if (mode === "verify") { await verifyPersistence(); return; }

  await register({ displayName: names.patient, email: emails.patient, password, role: "PATIENT" });
  const patientToken = await login(emails.patient, password);
  ok("patient registration and login", "patient account created and authenticated");

  await register({ displayName: names.doctorApproved, email: emails.doctorApproved, password, role: "DOCTOR", registrationNumber: `QA-DOC-A-${runId}` });
  const doctorToken = await login(emails.doctorApproved, password);
  await register({ displayName: names.doctorRejected, email: emails.doctorRejected, password, role: "DOCTOR", registrationNumber: `QA-DOC-R-${runId}` });
  ok("doctor registration and login", "doctor accounts created; approved doctor authenticated");

  await register({ displayName: names.hospital, email: emails.hospital, password, role: "HOSPITAL", facilityName: names.hospital, registrationNumber: `QA-HOSP-${runId}` });
  const hospitalToken = await login(emails.hospital, password);
  ok("hospital registration and login", "hospital account created and authenticated");

  const pendingSearch = await call("GET", `/doctors?q=${encodeURIComponent(names.doctorApproved)}`);
  ensure(pendingSearch.status === 200, "provider visibility", `public doctor search returned ${pendingSearch.status}`);
  ensure(pendingSearch.body.data.content.length === 0, "provider visibility", "pending doctor was publicly visible");
  ok("unapproved provider visibility", "pending doctor was hidden from public search");

  const adminToken = await login(adminEmail, adminPassword);
  const pendingDoctorsResponse = await call("GET", "/admin/doctors?status=PENDING", undefined, adminToken);
  const pendingHospitalsResponse = await call("GET", "/admin/hospitals?status=PENDING", undefined, adminToken);
  ensure(pendingDoctorsResponse.status === 200 && pendingHospitalsResponse.status === 200, "admin provider queues", "pending provider queues did not return 200");
  const approvedDoctor = pendingDoctorsResponse.body.data.content.find((doctor) => doctor.name === names.doctorApproved);
  const rejectedDoctor = pendingDoctorsResponse.body.data.content.find((doctor) => doctor.name === names.doctorRejected);
  const hospital = pendingHospitalsResponse.body.data.content.find((facility) => facility.name === names.hospital);
  ensure(approvedDoctor && rejectedDoctor && hospital, "admin provider queues", "registered provider profiles were not persisted");
  const approveDoctor = await call("PUT", `/admin/doctors/${approvedDoctor.id}/status`, { status: "APPROVED" }, adminToken);
  const rejectDoctor = await call("PUT", `/admin/doctors/${rejectedDoctor.id}/status`, { status: "REJECTED", reason: "QA verification rejection" }, adminToken);
  const approveHospital = await call("PUT", `/admin/hospitals/${hospital.id}/status`, { status: "APPROVED" }, adminToken);
  ensure(approveDoctor.status === 200 && rejectDoctor.status === 200 && approveHospital.status === 200, "provider decisions", "provider approval/rejection did not return 200");
  ok("provider approval and rejection", "admin approved one doctor and hospital, and rejected a separate doctor");

  const doctorSearch = await call("GET", `/doctors?q=${encodeURIComponent(names.doctorApproved)}`);
  const rejectedSearch = await call("GET", `/doctors?q=${encodeURIComponent(names.doctorRejected)}`);
  const hospitalSearch = await call("GET", `/hospitals?q=${encodeURIComponent(names.hospital)}`);
  ensure(doctorSearch.status === 200 && doctorSearch.body.data.content.some((doctor) => doctor.id === approvedDoctor.id), "doctor search", "approved doctor was not publicly visible");
  ensure(rejectedSearch.status === 200 && rejectedSearch.body.data.content.length === 0, "doctor search", "rejected doctor was publicly visible");
  ensure(hospitalSearch.status === 200 && hospitalSearch.body.data.content.some((facility) => facility.id === hospital.id), "hospital search", "approved hospital was not publicly visible");
  ok("doctor and hospital search", "only approved providers appeared in public search");

  const doctorMe = await call("GET", "/doctors/me", undefined, doctorToken);
  ensure(doctorMe.status === 200, "doctor dashboard", `expected 200, got ${doctorMe.status}`);
  const hospitalMe = await call("GET", "/hospitals/me", undefined, hospitalToken);
  ensure(hospitalMe.status === 200, "hospital dashboard", `expected 200, got ${hospitalMe.status}`);
  ok("role-based dashboard access", "doctor and hospital own-dashboard APIs returned 200");

  const patientAdmin = await call("GET", "/admin/dashboard", undefined, patientToken);
  const doctorPatientAppointments = await call("GET", "/appointments", undefined, doctorToken);
  ensure(patientAdmin.status === 403 && doctorPatientAppointments.status === 403, "role authorization", "cross-role routes were not forbidden");
  ok("role authorization", "patient cannot access admin; doctor cannot access patient appointments");

  const profileUpdate = await call("PUT", "/doctors/me", { qualification: "QA Medicine", biography: "QA audit profile", yearsOfExperience: 8, consultationFee: 500, city: "QA City", address: "QA Clinical Lane", profileImageUrl: null }, doctorToken);
  ensure(profileUpdate.status === 200, "doctor profile update", `expected 200, got ${profileUpdate.status}`);
  const start = new Date(Date.now() + 48 * 60 * 60 * 1000);
  start.setUTCMinutes(0, 0, 0);
  const end = new Date(start.getTime() + 30 * 60 * 1000);
  const slotCreate = await call("POST", "/doctors/me/slots", { startsAt: start.toISOString(), endsAt: end.toISOString() }, doctorToken);
  ensure(slotCreate.status === 200, "availability management", `expected 200, got ${slotCreate.status}`);
  const slot = slotCreate.body.data;
  const publicSlots = await call("GET", `/doctors/${approvedDoctor.id}/slots?date=${start.toISOString().slice(0, 10)}`);
  ensure(publicSlots.status === 200 && publicSlots.body.data.some((item) => item.id === slot.id), "availability management", "created slot was not publicly available");
  ok("doctor availability management", "approved doctor profile and available slot persisted");

  const booking = await call("POST", "/appointments", { slotId: slot.id, reason: "QA appointment booking" }, patientToken);
  ensure(booking.status === 200 && booking.body.data.status === "BOOKED", "appointment booking", `expected booked appointment, got ${booking.status}`);
  const patientAppointments = await call("GET", "/appointments", undefined, patientToken);
  ensure(patientAppointments.status === 200 && patientAppointments.body.data.some((appointment) => appointment.id === booking.body.data.id), "appointment booking", "booked appointment did not appear in patient dashboard");
  ok("appointment booking", "patient booking was saved and shown in the dashboard API");

  const cancellation = await call("POST", `/appointments/${booking.body.data.id}/cancel`, { reason: "QA cancellation check" }, patientToken);
  ensure(cancellation.status === 200 && cancellation.body.data.status === "CANCELLED", "appointment cancellation", `expected cancelled appointment, got ${cancellation.status}`);
  const releasedSlots = await call("GET", `/doctors/${approvedDoctor.id}/slots?date=${start.toISOString().slice(0, 10)}`);
  ensure(releasedSlots.status === 200 && releasedSlots.body.data.some((item) => item.id === slot.id), "appointment cancellation", "cancelled slot was not released");
  ok("appointment cancellation", "appointment was cancelled and slot was released");
}

try {
  await run();
} catch (error) {
  fail("audit runner", error instanceof Error ? error.message : "unknown error");
}

for (const result of results) console.log(`${result.ok ? "PASS" : "FAIL"}: ${result.name} — ${result.detail}`);
if (results.some((result) => !result.ok)) process.exitCode = 1;
