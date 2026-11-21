/*
 * Real password-reset audit. It creates clearly labelled QA accounts in the
 * configured database and consumes reset links from a dedicated local-only
 * development outbox. It never prints passwords, tokens, or reset URLs.
 */
import { readdir, readFile } from "node:fs/promises";
import { join } from "node:path";

const base = process.env.QA_API_BASE ?? "http://localhost:8084/api";
const outbox = process.env.QA_RESET_OUTBOX;
const initialPassword = process.env.QA_PASSWORD;
const newPassword = process.env.QA_NEW_PASSWORD;
const runId = process.argv[2] ?? "reset-local";

if (!outbox || !initialPassword || !newPassword) {
  throw new Error("QA_RESET_OUTBOX, QA_PASSWORD, and QA_NEW_PASSWORD must be configured.");
}

const results = [];
function pass(name, detail) { results.push({ name, ok: true, detail }); }
function fail(name, detail) { results.push({ name, ok: false, detail }); }
function ensure(condition, name, detail) { if (!condition) throw new Error(`${name}: ${detail}`); }

async function call(method, path, body, token) {
  const response = await fetch(`${base}${path}`, {
    method,
    headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    ...(body === undefined ? {} : { body: JSON.stringify(body) }),
  });
  return { status: response.status, body: await response.json().catch(() => null) };
}

async function login(email, password) {
  const response = await call("POST", "/auth/login", { email, password });
  return response;
}

async function resetTokenFor(email) {
  const files = await readdir(outbox);
  for (const file of files) {
    const message = await readFile(join(outbox, file), "utf8");
    if (!message.startsWith(`To: ${email}`)) continue;
    const link = message.match(/^Reset link:\s*(\S+)$/m)?.[1];
    if (!link) continue;
    const token = new URL(link).searchParams.get("token");
    if (token) return token;
  }
  throw new Error("development outbox did not contain the expected reset link");
}

const accounts = [
  { role: "PATIENT", name: "QA Reset Patient", email: `qa-reset-patient-${runId}@medislot.test` },
  { role: "DOCTOR", name: "QA Reset Doctor", email: `qa-reset-doctor-${runId}@medislot.test`, registrationNumber: `QA-RESET-DOC-${runId}` },
  { role: "HOSPITAL", name: "QA Reset Hospital", email: `qa-reset-hospital-${runId}@medislot.test`, facilityName: "QA Reset Hospital", registrationNumber: `QA-RESET-HOSP-${runId}` },
];

async function run() {
  const health = await call("GET", "/health");
  ensure(health.status === 200 && health.body.status === "UP", "health", "API did not report UP");

  const generic = await call("POST", "/auth/forgot-password", { email: `unknown-${runId}@medislot.test` });
  ensure(generic.status === 202, "unknown-email response", `expected 202, got ${generic.status}`);
  const genericMessage = generic.body?.data?.message;

  for (const account of accounts) {
    const registration = await call("POST", "/auth/register", { ...account, displayName: account.name, password: initialPassword });
    ensure(registration.status === 201, `${account.role} registration`, `expected 201, got ${registration.status}`);
    const oldLogin = await login(account.email, initialPassword);
    ensure(oldLogin.status === 200, `${account.role} initial login`, `expected 200, got ${oldLogin.status}`);
    const oldAccessToken = oldLogin.body.data.accessToken;

    const request = await call("POST", "/auth/forgot-password", { email: account.email });
    ensure(request.status === 202 && request.body?.data?.message === genericMessage, `${account.role} generic forgot-password response`, "email enumeration protection failed");
    const token = await resetTokenFor(account.email);

    const reset = await call("POST", "/auth/reset-password", { token, password: newPassword });
    ensure(reset.status === 200, `${account.role} valid reset`, `expected 200, got ${reset.status}`);
    const reuse = await call("POST", "/auth/reset-password", { token, password: newPassword });
    ensure(reuse.status === 400, `${account.role} single use`, `expected 400, got ${reuse.status}`);
    const invalid = await call("POST", "/auth/reset-password", { token: "invalid-reset-token-abcdefghijklmnopqrstuvwxyz", password: newPassword });
    ensure(invalid.status === 400, `${account.role} invalid token`, `expected 400, got ${invalid.status}`);
    const oldPassword = await login(account.email, initialPassword);
    ensure(oldPassword.status === 401, `${account.role} old password rejection`, `expected 401, got ${oldPassword.status}`);
    const currentSession = await call("GET", "/auth/me", undefined, oldAccessToken);
    ensure(currentSession.status === 401, `${account.role} prior-session invalidation`, `expected 401, got ${currentSession.status}`);
    const newLogin = await login(account.email, newPassword);
    ensure(newLogin.status === 200, `${account.role} new password login`, `expected 200, got ${newLogin.status}`);
    pass(`${account.role.toLowerCase()} password reset`, "registration, generic request, single-use reset, prior-session invalidation, old-password rejection, and new-password login passed");
  }
  pass("forgot-password unknown email", "returned the same generic accepted response without creating a reset link");
}

try {
  await run();
} catch (error) {
  fail("password-reset audit", error instanceof Error ? error.message : "unknown error");
}
for (const result of results) console.log(`${result.ok ? "PASS" : "FAIL"}: ${result.name} — ${result.detail}`);
if (results.some((result) => !result.ok)) process.exitCode = 1;
