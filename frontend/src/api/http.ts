export class ApiError extends Error {
  status: number;
  details?: any;
  constructor(status: number, message: string, details?: any) {
    super(message);
    this.status = status;
    this.details = details;
  }
}

export async function http<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`/api${path}`, {
    headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
    ...init,
  });

  const text = await res.text();
  if (!res.ok) {
    let details: any;
    try { details = text ? JSON.parse(text) : undefined; } catch {}
    const message = details?.message || text || res.statusText;
    throw new ApiError(res.status, message, details);
  }
  return text ? (JSON.parse(text) as T) : (undefined as T);
}
