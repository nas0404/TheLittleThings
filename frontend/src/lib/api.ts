//src/lib/api.ts

export const API_BASE = "/api"; //  http://localhost:8080/api
export const USER_ID = 1; // Will evebtually come from auth
export const USE_MOCK = true; // Start with mock; flip to false to hit Spring Boot


export async function http<T>(path: string, init?: RequestInit): Promise<T> {
const res = await fetch(`${API_BASE}${path}`, {
headers: { "Content-Type": "application/json" },
...init,
});
if (!res.ok) {
const text = await res.text().catch(() => "");
throw new Error(`API ${res.status}: ${text || res.statusText}`);
}
return (await res.json()) as T;
}


export const api = { http };