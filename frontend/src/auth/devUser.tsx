// src/auth/devUser.ts
const KEY = "devUserId";

export function setDevUserId(id: number) {
  localStorage.setItem(KEY, String(id));
}

export function getDevUserId(): number | null {
  const raw = localStorage.getItem(KEY);
  if (!raw) return null;
  const n = Number(raw);
  return Number.isFinite(n) && n > 0 ? n : null;
}

export function clearDevUserId() {
  localStorage.removeItem(KEY);
}