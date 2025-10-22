// src/api/http.ts

// Unified error type
export class ApiError extends Error {
  status: number;
  details?: any;
  constructor(status: number, message: string, details?: any) {
    super(message);
    this.status = status;
    this.details = details;
  }
}

/**
 * Optional base URL - uses same-origin in production
 */
const BASE = (import.meta.env.VITE_API_BASE || "").replace(/\/+$/, "");

/**
 * Options you can pass to http()
 *  - skipAuth: do not attach Authorization header (useful for login/register)
 */
type HttpOptions = RequestInit & { skipAuth?: boolean };

export async function http<T = unknown>(path: string, init: HttpOptions = {}): Promise<T> {
  // Normalize URL
  const rel = path.startsWith("/") ? path : `/${path}`;
  const url = `${BASE}${rel}`;

  // Build headers
  const headers = new Headers(init.headers || {});
  // Only add JSON content-type if we actually send a body and caller didn't set it
  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  // Attach token unless explicitly skipped
  const token = localStorage.getItem("token");
  if (!init.skipAuth && token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  // Make request (include credentials so cookies work if you ever use them)
  const res = await fetch(url, {
    credentials: "include",
    ...init,
    headers,
  });

  // Read body safely (could be empty or non-JSON)
  const text = await res.text();
  const tryJson = () => {
    try { return text ? JSON.parse(text) : undefined; } catch { return undefined; }
  };

  if (!res.ok) {
    const details = tryJson();
    const message =
      (details && (details.message || details.error)) ||
      (text || res.statusText || `HTTP ${res.status}`);
    throw new ApiError(res.status, message, details);
  }

  // 204 / empty body
  if (!text) return undefined as T;

  // Prefer JSON, fall back to text
  const json = tryJson();
  return (json ?? (text as unknown)) as T;
}

/* Optional helpers if you want them here */
export function saveAuth(token: string, username?: string) {
  localStorage.setItem("token", token);
  if (username) localStorage.setItem("username", username);
}
export function clearAuth() {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
}
