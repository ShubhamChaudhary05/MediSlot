"use client";

import Link from "next/link";
import { Building2, Search } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { ApiError, apiGet } from "@/lib/api";

type Hospital = { id: string; name: string; city?: string; address?: string };
type Page<T> = { content: T[] };

export default function FindHospitalsPage() {
  const [query, setQuery] = useState(""); const [city, setCity] = useState(""); const [results, setResults] = useState<Hospital[]>([]); const [loading, setLoading] = useState(true); const [error, setError] = useState<string | null>(null);
  const search = useCallback(async (event?: React.FormEvent) => {
    event?.preventDefault(); setLoading(true); setError(null);
    try { const params = new URLSearchParams({ size: "24" }); if (query.trim()) params.set("q", query.trim()); if (city.trim()) params.set("city", city.trim()); setResults((await apiGet<Page<Hospital>>(`/hospitals?${params}`)).content); }
    catch (requestError) { setError(requestError instanceof ApiError ? requestError.message : "Unable to load hospitals right now."); }
    finally { setLoading(false); }
  }, [city, query]);
  useEffect(() => { void search(); }, [search]);
  return <main className="min-h-screen px-5 py-8 sm:px-8"><div className="mx-auto max-w-6xl"><Link className="text-sm font-bold text-blue-700" href="/">← MediSlot home</Link><div className="mt-8"><p className="text-sm font-bold uppercase tracking-wider text-blue-700">Verified facilities</p><h1 className="mt-1 text-4xl font-black tracking-tight text-slate-950">Find a hospital or clinic</h1><p className="mt-2 text-slate-600">Only administrator-approved facilities are listed.</p></div><form onSubmit={search} className="mt-8 grid gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-[1fr_1fr_auto]"><input value={query} onChange={event => setQuery(event.target.value)} aria-label="Search facilities" className="rounded-xl border border-slate-200 px-3 py-3" placeholder="Hospital or clinic name" /><input value={city} onChange={event => setCity(event.target.value)} aria-label="Filter by city" className="rounded-xl border border-slate-200 px-3 py-3" placeholder="City" /><button className="inline-flex items-center justify-center gap-2 rounded-xl bg-slate-900 px-5 py-3 font-bold text-white"><Search size={18} /> Search</button></form>{error && <p role="alert" className="mt-6 rounded-xl bg-red-50 p-4 text-red-800">{error}</p>}{loading ? <div className="mt-8 grid gap-4 md:grid-cols-2 lg:grid-cols-3">{[1, 2, 3].map(item => <div key={item} className="h-40 animate-pulse rounded-2xl bg-slate-200" />)}</div> : results.length ? <div className="mt-8 grid gap-4 md:grid-cols-2 lg:grid-cols-3">{results.map(hospital => <article key={hospital.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"><Building2 className="text-blue-700" /><h2 className="mt-4 text-lg font-extrabold text-slate-950">{hospital.name}</h2><p className="mt-2 text-sm text-slate-600">{hospital.address || "Address to be updated"}</p><p className="mt-3 text-sm font-semibold text-blue-700">{hospital.city || "Location to be updated"}</p></article>)}</div> : <div className="mt-8 rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center"><Building2 className="mx-auto text-slate-400" size={32} /><h2 className="mt-3 font-bold text-slate-900">No facilities match those filters</h2><p className="mt-1 text-sm text-slate-600">Try another city or name. New facilities are published after verification.</p></div>}</div></main>;
}
