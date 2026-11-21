"use client";

import { Eye, EyeOff, LockKeyhole } from "lucide-react";
import { useId, useState } from "react";

type PasswordFieldProps = {
  name: string;
  label: string;
  autoComplete: string;
  placeholder?: string;
  minLength?: number;
  required?: boolean;
};

export function PasswordField({ name, label, autoComplete, placeholder = "At least 12 characters", minLength = 12, required = true }: PasswordFieldProps) {
  const [visible, setVisible] = useState(false);
  const id = useId();
  return <label htmlFor={id} className="block text-sm font-semibold text-slate-700">{label}<span className="relative mt-2 block"><LockKeyhole aria-hidden="true" className="absolute left-3 top-3 text-slate-400" size={18} /><input id={id} name={name} type={visible ? "text" : "password"} required={required} minLength={minLength} autoComplete={autoComplete} className="w-full rounded-xl border border-slate-200 py-3 pl-10 pr-12 font-normal outline-none focus:border-blue-500" placeholder={placeholder} /><button type="button" onClick={() => setVisible((current) => !current)} className="absolute right-2 top-2 rounded-lg p-1.5 text-slate-500 hover:bg-slate-100 hover:text-blue-700" aria-label={visible ? `Hide ${label.toLowerCase()}` : `Show ${label.toLowerCase()}`} aria-pressed={visible}>{visible ? <EyeOff aria-hidden="true" size={19} /> : <Eye aria-hidden="true" size={19} />}</button></span></label>;
}
