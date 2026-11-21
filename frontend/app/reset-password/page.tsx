"use client";

import Link from "next/link";
import { ArrowLeft, HeartPulse } from "lucide-react";
import { Suspense, useState } from "react";
import { useSearchParams } from "next/navigation";
import { PasswordField } from "@/components/PasswordField";
import { ApiError, resetPassword } from "@/lib/api";

function ResetPasswordForm() {
  const params = useSearchParams(); const token = params.get("token") ?? ""; const [busy, setBusy] = useState(false); const [message, setMessage] = useState<string | null>(null); const [success, setSuccess] = useState(false);
  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault(); const form = new FormData(event.currentTarget); const password = String(form.get("password") ?? "");
    if (!token) { setMessage("This password-reset link is invalid or incomplete."); return; }
    if (password !== String(form.get("confirmPassword") ?? "")) { setMessage("Passwords do not match."); return; }
    setBusy(true); setMessage(null);
    try { const response = await resetPassword(token, password); setSuccess(true); setMessage(response.message); }
    catch (error) { setMessage(error instanceof ApiError ? error.message : "Unable to reset your password right now."); }
    finally { setBusy(false); }
  }
  return <main className="flex min-h-screen items-center justify-center px-4 py-10"><section className="w-full max-w-md rounded-3xl border border-slate-200 bg-white p-6 shadow-xl shadow-slate-200/60 sm:p-8"><Link href="/auth/patient" className="inline-flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-blue-700"><ArrowLeft size={16} /> Back to sign in</Link><div className="mt-6 flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-600 text-white"><HeartPulse size={25} /></div><h1 className="mt-5 text-2xl font-extrabold text-slate-950">Choose a new password</h1><form onSubmit={submit} className="mt-6 space-y-4"><PasswordField name="password" label="New password" autoComplete="new-password" /><PasswordField name="confirmPassword" label="Confirm new password" autoComplete="new-password" />{message && <p role="status" className={`rounded-xl p-3 text-sm leading-5 ${success ? "bg-emerald-50 text-emerald-900" : "bg-slate-100 text-slate-700"}`}>{message}</p>}{success ? <Link href="/auth/patient" className="block rounded-xl bg-blue-600 px-4 py-3 text-center font-bold text-white">Sign in with your new password</Link> : <button disabled={busy} className="w-full rounded-xl bg-blue-600 px-4 py-3 font-bold text-white shadow-lg shadow-blue-200 disabled:cursor-not-allowed disabled:opacity-60">{busy ? "Resetting…" : "Reset password"}</button>}</form></section></main>;
}

export default function ResetPasswordPage() { return <Suspense fallback={<main className="flex min-h-screen items-center justify-center text-slate-600">Loading reset form…</main>}><ResetPasswordForm /></Suspense>; }
