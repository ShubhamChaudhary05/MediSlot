"use client";

import Link from "next/link";
import { Search, Stethoscope } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { ApiError, apiGet } from "@/lib/api";

type Doctor = { id: string; name: string; qualification?: string; yearsOfExperience: number; consultationFee: number; city?: string; imageUrl?: string };
type Page<T> = { content: T[]; totalElements: number };

export default function FindDoctorsPage() {
  const [query, setQuery] = useState(""); const [city, setCity] = useState("");
  const [doctors, setDoctors] = useState<Doctor[]>([]); const [loading, setLoading] = useState(true); const [error, setError] = useState<string | null>(null);
  const search = useCallback(async (event?: React.FormEvent) => {
    event?.preventDefault(); setLoading(true); setError(null);
    try {
      const params = new URLSearchParams({ size: "24" }); if (query.trim()) params.set("q", query.trim()); if (city.trim()) params.set("city", city.trim());
      const result = await apiGet<Page<Doctor>>(`/doctors?${params}`); setDoctors(result.content);
    } catch (requestError) { setError(requestError instanceof ApiError ? requestError.message : "Unable to load doctors right now."); }
    finally { setLoading(false); }
  }, [city, query]);
  useEffect(() => { void search(); }, [search]);
  return <main className="min-h-screen px-5 py-8 sm:px-8"><div className="mx-auto max-w-6xl"><Link className="text-sm font-bold text-blue-700" href="/">← MediSlot home</Link><div className="mt-8 flex flex-col justify-between gap-4 sm:flex-row sm:items-end"><div><p className="text-sm font-bold uppercase tracking-wider text-blue-700">Verified care</p><h1 className="mt-1 text-4xl font-black tracking-tight text-slate-950">Find a doctor</h1><p className="mt-2 text-slate-600">Only administrator-approved providers appear here.</p></div><Link href="/auth/patient" className="rounded-xl bg-blue-600 px-4 py-3 text-center text-sm font-bold text-white">Create patient account</Link></div><form onSubmit={search} className="mt-8 grid gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-[1fr_1fr_auto]"><input value={query} onChange={e => setQuery(e.target.value)} aria-label="Search doctors" className="rounded-xl border border-slate-200 px-3 py-3" placeholder="Name or qualification" /><input value={city} onChange={e => setCity(e.target.value)} aria-label="Filter by city" className="rounded-xl border border-slate-200 px-3 py-3" placeholder="City" /><button className="inline-flex items-center justify-center gap-2 rounded-xl bg-slate-900 px-5 py-3 font-bold text-white"><Search size={18} /> Search</button></form>{error && <p role="alert" className="mt-6 rounded-xl bg-red-50 p-4 text-red-800">{error}</p>}{loading ? <div className="mt-8 grid gap-4 md:grid-cols-2 lg:grid-cols-3">{[1,2,3].map(n => <div key={n} className="h-48 animate-pulse rounded-2xl bg-slate-200" />)}</div> : doctors.length === 0 ? <div className="mt-8 rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center"><Stethoscope className="mx-auto text-slate-400" size={32} /><h2 className="mt-3 font-bold text-slate-900">No doctors match those filters</h2><p className="mt-1 text-sm text-slate-600">Try another city or search term. New providers appear after verification.</p></div> : <div className="mt-8 grid gap-4 md:grid-cols-2 lg:grid-cols-3">{doctors.map(doctor => <article key={doctor.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"><div className="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-blue-700"><Stethoscope size={22} /></div><h2 className="mt-4 text-lg font-extrabold text-slate-950">{doctor.name}</h2><p className="mt-1 text-sm text-slate-600">{doctor.qualification || "Medical practitioner"}</p><dl className="mt-4 grid grid-cols-2 gap-3 text-sm"><div><dt className="text-slate-500">Experience</dt><dd className="font-bold">{doctor.yearsOfExperience} years</dd></div><div><dt className="text-slate-500">Consultation</dt><dd className="font-bold">₹{doctor.consultationFee}</dd></div></dl><p className="mt-3 text-sm text-slate-600">{doctor.city || "Location to be updated"}</p><Link href={`/doctors/${doctor.id}`} className="mt-5 block rounded-xl border border-blue-200 px-4 py-2.5 text-center text-sm font-bold text-blue-700">View availability</Link></article>)}</div>}</div></main>;
}
