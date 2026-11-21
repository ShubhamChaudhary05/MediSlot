"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { ArrowLeft, Building2, HeartPulse, Mail, Stethoscope, UserRound } from "lucide-react";
import { useState } from "react";
import { ApiError, login, register, saveSession, type UserRole } from "@/lib/api";
import { PasswordField } from "@/components/PasswordField";

type EntryPoint = "patient" | "provider";

export default function AuthPage() {
  const params = useParams<{ role: string }>();
  const directProviderType = params.role === "doctor" ? "DOCTOR" : params.role === "hospital" ? "HOSPITAL" : null;
  const entryPoint: EntryPoint = params.role === "provider" || directProviderType ? "provider" : "patient";
  const isProvider = entryPoint === "provider";
  const [mode, setMode] = useState<"login" | "signup">("signup");
  const [providerType, setProviderType] = useState<"DOCTOR" | "HOSPITAL">(directProviderType ?? "DOCTOR");
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const router = useRouter();

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setMessage(null); setBusy(true);
    try {
      const email = String(form.get("email") ?? "");
      const password = String(form.get("password") ?? "");
      if (mode === "login") {
        saveSession(await login(email, password));
        router.push("/dashboard");
        return;
      }
      if (password !== String(form.get("confirmPassword") ?? "")) {
        setMessage("Passwords do not match.");
        return;
      }
      const accountRole: UserRole = isProvider ? providerType : "PATIENT";
      await register({
        displayName: String(form.get("displayName") ?? ""), email, password, role: accountRole,
        facilityName: providerType === "HOSPITAL" ? String(form.get("facilityName") ?? "") : undefined,
        registrationNumber: isProvider ? String(form.get("registrationNumber") ?? "") : undefined,
      });
      setMode("login");
      setMessage("Account created. You can now log in. Provider accounts remain hidden until an administrator approves them.");
    } catch (error) {
      setMessage(error instanceof ApiError ? error.message : "Unable to reach MediSlot. Please try again.");
    } finally { setBusy(false); }
  }

  return (
    <main className="flex min-h-screen items-center justify-center px-4 py-10">
      <div className="w-full max-w-md">
        <Link href="/" className="mb-6 inline-flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-blue-700"><ArrowLeft size={16} /> Back to MediSlot</Link>
        <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-xl shadow-slate-200/60 sm:p-8">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-600 text-white"><HeartPulse size={25} /></div>
          <p className="mt-5 text-sm font-bold uppercase tracking-wider text-blue-700">{directProviderType === "DOCTOR" ? "Doctor account" : directProviderType === "HOSPITAL" ? "Hospital account" : isProvider ? "Provider account" : "Patient account"}</p>
          <h1 className="mt-2 text-2xl font-extrabold text-slate-950">{mode === "signup" ? "Create your account" : "Welcome back"}</h1>
          <p className="mt-2 text-sm leading-6 text-slate-500">{isProvider ? "For doctors and authorized hospital representatives." : "Sign in or create an account to manage your healthcare journey."}</p>

          <div className="mt-6 grid grid-cols-2 rounded-xl bg-slate-100 p-1">
            <button type="button" onClick={() => { setMode("signup"); setMessage(null); }} className={`rounded-lg px-3 py-2.5 text-sm font-bold ${mode === "signup" ? "bg-white text-blue-700 shadow-sm" : "text-slate-500"}`}>Sign up</button>
            <button type="button" onClick={() => { setMode("login"); setMessage(null); }} className={`rounded-lg px-3 py-2.5 text-sm font-bold ${mode === "login" ? "bg-white text-blue-700 shadow-sm" : "text-slate-500"}`}>Log in</button>
          </div>

          <form className="mt-6 space-y-4" onSubmit={submit}>
            {mode === "signup" && <label className="block text-sm font-semibold text-slate-700">{providerType === "HOSPITAL" && isProvider ? "Representative name" : "Full name"}<span className="relative mt-2 block"><UserRound className="absolute left-3 top-3 text-slate-400" size={18} /><input name="displayName" required autoComplete="name" className="w-full rounded-xl border border-slate-200 py-3 pl-10 pr-3 font-normal outline-none focus:border-blue-500" placeholder="Enter your full name" /></span></label>}
            {mode === "signup" && isProvider && !directProviderType && <div className="grid grid-cols-2 gap-2 rounded-xl bg-slate-100 p-1"><button type="button" onClick={() => setProviderType("DOCTOR")} className={`flex items-center justify-center gap-2 rounded-lg px-2 py-2 text-sm font-bold ${providerType === "DOCTOR" ? "bg-white text-blue-700 shadow-sm" : "text-slate-500"}`}><Stethoscope size={16} /> Doctor</button><button type="button" onClick={() => setProviderType("HOSPITAL")} className={`flex items-center justify-center gap-2 rounded-lg px-2 py-2 text-sm font-bold ${providerType === "HOSPITAL" ? "bg-white text-blue-700 shadow-sm" : "text-slate-500"}`}><Building2 size={16} /> Facility</button></div>}
            {mode === "signup" && isProvider && providerType === "HOSPITAL" && <label className="block text-sm font-semibold text-slate-700">Facility name<input name="facilityName" required className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-3 font-normal outline-none focus:border-blue-500" placeholder="MediCare Clinic" /></label>}
            {mode === "signup" && isProvider && <label className="block text-sm font-semibold text-slate-700">Registration / license number<input name="registrationNumber" required className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-3 font-normal outline-none focus:border-blue-500" placeholder="Enter your registration number" /></label>}
            <label className="block text-sm font-semibold text-slate-700">Email address<span className="relative mt-2 block"><Mail className="absolute left-3 top-3 text-slate-400" size={18} /><input name="email" type="email" required autoComplete="email" className="w-full rounded-xl border border-slate-200 py-3 pl-10 pr-3 font-normal outline-none focus:border-blue-500" placeholder="you@example.com" /></span></label>
            <PasswordField name="password" label="Password" autoComplete={mode === "signup" ? "new-password" : "current-password"} />
            {mode === "signup" && <PasswordField name="confirmPassword" label="Confirm password" autoComplete="new-password" />}
            {mode === "login" && <div className="text-right"><Link href="/forgot-password" className="text-sm font-bold text-blue-700 hover:text-blue-800">Forgot password?</Link></div>}
            {message && <p role="status" className="rounded-xl bg-slate-100 p-3 text-sm leading-5 text-slate-700">{message}</p>}
            <button disabled={busy} type="submit" className="flex w-full items-center justify-center rounded-xl bg-blue-600 px-4 py-3 font-bold text-white shadow-lg shadow-blue-200 transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60">{busy ? "Please wait…" : mode === "signup" ? "Create account" : "Log in"}</button>
          </form>
          <p className="mt-5 text-center text-xs leading-5 text-slate-500">Passwords are hashed by the MediSlot API. Provider listings require administrator approval.</p>
        </section>
      </div>
    </main>
  );
}
