import Link from "next/link";
import { Activity, ArrowRight, CalendarCheck, HeartPulse, ShieldCheck, Stethoscope, UserRound } from "lucide-react";

const benefits = [
  { icon: CalendarCheck, title: "Simple appointment booking", description: "A clear path from finding a provider to choosing an appointment time." },
  { icon: ShieldCheck, title: "Provider verification", description: "Provider listings will be published only after the required review process is implemented." },
  { icon: Activity, title: "Less waiting, more clarity", description: "Future phases will add schedules and visit-window estimates based on real booking data." },
];

export default function HomePage() {
  return (
    <main className="min-h-screen overflow-hidden">
      <header className="mx-auto flex max-w-7xl items-center justify-between px-5 py-5 sm:px-8">
        <Link href="/" className="flex items-center gap-3" aria-label="MediSlot home">
          <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-blue-600 text-white shadow-lg shadow-blue-200">
            <HeartPulse size={25} />
          </span>
          <span>
            <span className="block text-xl font-extrabold tracking-tight">MediSlot</span>
            <span className="block text-xs text-slate-500">Book smarter, wait shorter.</span>
          </span>
        </Link>
        <nav className="flex items-center gap-3 text-sm font-semibold">
          <Link href="/find-doctors" className="hidden text-slate-600 hover:text-blue-700 sm:inline">Find doctors</Link>
          <Link href="/find-hospitals" className="hidden text-slate-600 hover:text-blue-700 md:inline">Find hospitals</Link>
          <Link href="/dashboard" className="rounded-full border border-blue-100 bg-white px-4 py-2 text-blue-700">My dashboard</Link>
        </nav>
      </header>

      <section className="mx-auto grid max-w-7xl gap-12 px-5 pb-14 pt-10 sm:px-8 md:grid-cols-[1.05fr_0.95fr] md:items-center md:pb-24 md:pt-16">
        <div>
          <div className="mb-5 inline-flex items-center gap-2 rounded-full border border-blue-100 bg-white px-3 py-1.5 text-sm font-medium text-blue-700">
            <span className="h-2 w-2 rounded-full bg-emerald-500" /> Your care journey starts here
          </div>
          <h1 className="max-w-2xl text-4xl font-black leading-tight tracking-tight text-slate-950 sm:text-5xl lg:text-6xl">
            Better care begins with the <span className="text-blue-600">right connection.</span>
          </h1>
          <p className="mt-5 max-w-xl text-base leading-7 text-slate-600 sm:text-lg">
            Find healthcare providers, explore their professional details, and manage appointments in one place.
          </p>
          <div className="mt-8 flex flex-wrap gap-3 text-sm text-slate-600">
            <span className="rounded-full bg-white px-4 py-2 shadow-sm">✓ Clear provider profiles</span>
            <span className="rounded-full bg-white px-4 py-2 shadow-sm">✓ Easier appointment planning</span>
            <Link href="/find-doctors" className="rounded-full bg-blue-600 px-4 py-2 font-bold text-white shadow-sm hover:bg-blue-700">Explore doctors</Link>
          </div>
        </div>

        <div className="relative">
          <div className="absolute -inset-4 rounded-[2.5rem] bg-gradient-to-br from-blue-100 via-sky-50 to-emerald-50 blur-xl" />
          <div className="relative rounded-[2rem] border border-white bg-white/90 p-5 shadow-xl shadow-blue-100/70 sm:p-7">
            <div className="mb-6">
              <p className="text-sm font-semibold text-blue-700">GET STARTED</p>
              <h2 className="mt-2 text-2xl font-extrabold text-slate-950">How would you like to continue?</h2>
              <p className="mt-2 text-sm leading-6 text-slate-500">Choose the account type that fits you.</p>
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <Link href="/auth/provider" className="group rounded-2xl border border-blue-100 bg-blue-50/70 p-5 transition hover:-translate-y-1 hover:border-blue-300 hover:bg-blue-50">
                <span className="flex h-12 w-12 items-center justify-center rounded-xl bg-blue-600 text-white shadow-md shadow-blue-200">
                  <Stethoscope size={24} />
                </span>
                <h3 className="mt-4 text-lg font-bold text-slate-950">Doctor / Hospital</h3>
                <p className="mt-2 min-h-12 text-sm leading-5 text-slate-600">Create a professional account and prepare your listing.</p>
                <span className="mt-5 inline-flex items-center gap-2 text-sm font-bold text-blue-700">Continue as provider <ArrowRight size={16} className="transition group-hover:translate-x-1" /></span>
              </Link>
              <Link href="/auth/patient" className="group rounded-2xl border border-emerald-100 bg-emerald-50/70 p-5 transition hover:-translate-y-1 hover:border-emerald-300 hover:bg-emerald-50">
                <span className="flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-600 text-white shadow-md shadow-emerald-200">
                  <UserRound size={24} />
                </span>
                <h3 className="mt-4 text-lg font-bold text-slate-950">Patient</h3>
                <p className="mt-2 min-h-12 text-sm leading-5 text-slate-600">Create an account to find providers and plan visits.</p>
                <span className="mt-5 inline-flex items-center gap-2 text-sm font-bold text-emerald-700">Continue as patient <ArrowRight size={16} className="transition group-hover:translate-x-1" /></span>
              </Link>
            </div>
            <p className="mt-5 text-center text-xs leading-5 text-slate-500">Secure accounts, provider approval, and API-backed appointment slots.</p>
          </div>
        </div>
      </section>

      <section className="border-y border-slate-200/70 bg-white/70">
        <div className="mx-auto grid max-w-7xl gap-6 px-5 py-10 sm:px-8 md:grid-cols-3">
          {benefits.map(({ icon: Icon, title, description }) => (
            <div key={title} className="flex gap-4">
              <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-blue-700"><Icon size={22} /></span>
              <div><h3 className="font-bold text-slate-900">{title}</h3><p className="mt-1 text-sm leading-6 text-slate-600">{description}</p></div>
            </div>
          ))}
        </div>
      </section>
      <footer className="mx-auto flex max-w-7xl flex-col gap-2 px-5 py-7 text-xs text-slate-500 sm:flex-row sm:justify-between sm:px-8">
        <span>© {new Date().getFullYear()} MediSlot</span>
        <span>Healthcare discovery and appointment planning</span>
      </footer>
    </main>
  );
}
