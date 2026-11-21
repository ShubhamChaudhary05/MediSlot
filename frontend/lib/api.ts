export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

export type UserRole = "PATIENT" | "DOCTOR" | "HOSPITAL" | "ADMIN";
export type User = { id: string; email: string; displayName: string; role: UserRole; providerStatus?: string | null };
export type Session = { accessToken: string; expiresAt: string; user: User };
export type ApiResult<T> = { data: T; timestamp: string };

export class ApiError extends Error {
  constructor(public status: number, public code: string, message: string, public fields: Record<string, string> = {}) { super(message); }
}

async function request<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers } });
  const body = await response.json().catch(() => null) as ApiResult<T> | { code?: string; message?: string; fields?: Record<string, string> } | null;
  if (!response.ok) {
    const error = body as { code?: string; message?: string; fields?: Record<string, string> } | null;
    throw new ApiError(response.status, error?.code ?? "REQUEST_FAILED", error?.message ?? "The request could not be completed.", error?.fields ?? {});
  }
  return (body as ApiResult<T>).data;
}

export async function register(input: { displayName: string; email: string; password: string; role: UserRole; facilityName?: string; registrationNumber?: string }) { return request<User>("/auth/register", { method: "POST", body: JSON.stringify(input) }); }
export async function login(email: string, password: string): Promise<Session> {
  const response = await request<{ accessToken: string; expiresAt: string; user: User }>("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) });
  return { accessToken: response.accessToken, expiresAt: response.expiresAt, user: response.user };
}
export async function logout(token: string) { await request<void>("/auth/logout", { method: "POST" }, token); }
export async function requestPasswordReset(email: string) { return request<{ message: string }>("/auth/forgot-password", { method: "POST", body: JSON.stringify({ email }) }); }
export async function resetPassword(token: string, password: string) { return request<{ message: string }>("/auth/reset-password", { method: "POST", body: JSON.stringify({ token, password }) }); }
export async function apiGet<T>(path: string, token?: string) { return request<T>(path, { cache: "no-store" }, token); }
export async function apiPost<T>(path: string, payload: unknown, token: string) { return request<T>(path, { method: "POST", body: JSON.stringify(payload) }, token); }

const SESSION_KEY = "medislot.session";
export function readSession(): Session | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.sessionStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    const session = JSON.parse(raw) as Session;
    if (new Date(session.expiresAt).getTime() <= Date.now()) { window.sessionStorage.removeItem(SESSION_KEY); return null; }
    return session;
  } catch { return null; }
}
export function saveSession(session: Session) { window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(session)); }
export function clearSession() { if (typeof window !== "undefined") window.sessionStorage.removeItem(SESSION_KEY); }
