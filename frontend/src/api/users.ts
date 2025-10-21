// frontend/src/api/users.ts
import { useEffect, useState } from "react";
import { http } from "./http";

const API_BASE = "/api/accounts"; // CHANGED: was /api/users

export type MeResponse = {
  userId: number;
  username: string;
  streaks?: number | null;
  trophies?: number | null;
};

export type LoginRequest = {
  usernameOrEmail: string;
  password: string;
};

export type RegisterRequest = {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  dob: string;
  gender: string;
  region: string;
};

export type LoginResponse = {
  token: string;
  userId: number;
  username: string;
};

export type UpdateUserRequest = {
  username?: string;
  email?: string;
  password?: string;
};

// Authentication and user management API
export const UserAPI = {
  async me(): Promise<MeResponse> {
    return http<MeResponse>(`${API_BASE}/me`);
  },

  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const r = await http<LoginResponse>(`${API_BASE}/login`, {
      method: "POST",
      body: JSON.stringify(credentials),
      skipAuth: true, // unauthenticated call
    });
    localStorage.setItem("token", r.token);
    localStorage.setItem("username", r.username);
    return r;
  },

  async register(userData: RegisterRequest): Promise<LoginResponse> {
    const r = await http<LoginResponse>(`${API_BASE}/register`, {
      method: "POST",
      body: JSON.stringify(userData),
      skipAuth: true, // unauthenticated call
    });
    localStorage.setItem("token", r.token);
    localStorage.setItem("username", r.username);
    return r;
  },

  async changeUsername(newUsername: string): Promise<LoginResponse> {
    const r = await http<LoginResponse>(`${API_BASE}/change-username`, {
      method: "POST",
      body: JSON.stringify({ newUsername }),
    });
    localStorage.setItem("token", r.token); // backend issues a new token
    localStorage.setItem("username", r.username);
    return r;
  },

  async changePassword(payload: { oldPassword: string; newPassword: string }): Promise<void> {
    await http<void>(`${API_BASE}/change-password`, {
      method: "POST",
      body: JSON.stringify(payload),
    });
  },

  async resetPassword(email: string): Promise<void> {
    await http<void>(`${API_BASE}/reset-password`, {
      method: "POST",
      body: JSON.stringify({ email }),
      skipAuth: true,
    });
  },

  async deleteMe(): Promise<void> {
    await http<void>(`${API_BASE}/`, { method: "DELETE" });
    localStorage.removeItem("token");
    localStorage.removeItem("username");
  },

  logout(): void {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
  },
};

export function useMe() {
  const [me, setMe] = useState<MeResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const data = await UserAPI.me();
        if (!cancelled) {
          setMe(data);
          localStorage.setItem("username", data.username);
        }
      } catch (e: any) {
        if (!cancelled) {
          setErr(e?.message || "Failed to load user");
          setMe(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  return { me, loading, err };
}
