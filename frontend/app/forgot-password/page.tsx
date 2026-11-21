"use client";

import Link from "next/link";
import { ArrowLeft, HeartPulse, Mail } from "lucide-react";
import { useState } from "react";
import { ApiError, requestPasswordReset } from "@/lib/api";

export default function ForgotPasswordPage() {
  const [busy, setBusy] = useState(false); const [message, setMessage] = useState<string | null>(null);
  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setMessage(null);
    try { const form = new FormData(event.currentTarget); const response = await requestPasswordReset(String(form.get("email") ?? "")); setMessage(response.message); }
    catch (error) { setMessage(error instanceof ApiError ? error.message : "Unable to submit your request right now."); }
    finally { setBusy(false); }
  }
  return <main className="flex min-h-screen items-center justify-center px-4 py-10"><section className="w-full max-w-md rounded-3xl border border-slate-200 bg-white p-6 shadow-xl shadow-slate-200/60 sm:p-8"><Link href="/auth/patient" className="inline-flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-blue-700"><ArrowLeft size={16} /> Back to sign in</Link><div className="mt-6 flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-600 text-white"><HeartPulse size={25} /></div><h1 className="mt-5 text-2xl font-extrabold text-slate-950">Reset your password</h1><p className="mt-2 text-sm leading-6 text-slate-600">Enter your email address. For security, the response is the same whether or not an account exists.</p><form onSubmit={submit} className="mt-6 space-y-4"><label className="block text-sm font-semibold text-slate-700">Email address<span className="relative mt-2 block"><Mail aria-hidden="true" className="absolute left-3 top-3 text-slate-400" size={18} /><input name="email" type="email" required autoComplete="email" className="w-full rounded-xl border border-slate-200 py-3 pl-10 pr-3 font-normal outline-none focus:border-blue-500" placeholder="you@example.com" /></span></label>{message && <p role="status" className="rounded-xl bg-slate-100 p-3 text-sm leading-5 text-slate-700">{message}</p>}<button disabled={busy} className="w-full rounded-xl bg-blue-600 px-4 py-3 font-bold text-white shadow-lg shadow-blue-200 disabled:cursor-not-allowed disabled:opacity-60">{busy ? "Submitting…" : "Send reset instructions"}</button></form></section></main>;
}
