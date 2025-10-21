import { useEffect, useState } from "react"; 
import { http } from "./http";

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
    return http<MeResponse>(`/api/users/me`);
  },

  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const r = await http<LoginResponse>(`/api/users/login`, {
      method: "POST",
      body: JSON.stringify(credentials),
      skipAuth: true,                 // CHANGED: unauthenticated call
    });
    localStorage.setItem("token", r.token);  // CHANGED: save token centrally
    localStorage.setItem("username", r.username); // CHANGED
    return r;
  },

  async register(userData: RegisterRequest): Promise<LoginResponse> {
    const r = await http<LoginResponse>(`/api/users/register`, {
      method: "POST",
      body: JSON.stringify(userData),
      skipAuth: true,                 // CHANGED: unauthenticated call
    });
    localStorage.setItem("token", r.token);  // CHANGED
    localStorage.setItem("username", r.username); // CHANGED
    return r;
  },


  async changeUsername(newUsername: string): Promise<LoginResponse> {
      const r = await http<LoginResponse>(`/api/users/change-username`, {
        method: "POST",
        body: JSON.stringify({ newUsername }),
      });
      localStorage.setItem("token", r.token);     // CHANGED: backend issues new token
      localStorage.setItem("username", r.username);
      return r;
    },

    async changePassword(payload: { oldPassword: string; newPassword: string }): Promise<void> {
      await http<void>(`/api/users/change-password`, {
        method: "POST",
        body: JSON.stringify(payload),
      });
    },

    async resetPassword(email: string): Promise<void> {
      await http<void>(`/api/users/reset-password`, {
        method: "POST",
        body: JSON.stringify({ email }),
        skipAuth: true,                 // CHANGED
      });
    },

    async deleteMe(): Promise<void> {
      await http<void>(`/api/users/`, { method: "DELETE" });
      localStorage.removeItem("token");           // CHANGED: clean up here
      localStorage.removeItem("username");
    },

    logout(): void {
      localStorage.removeItem("token");
      localStorage.removeItem("username");
    },
};

export function useMe() {                      // ADDED
  const [me, setMe] = useState<MeResponse | null>(null);     // ADDED
  const [loading, setLoading] = useState(true);               // ADDED
  const [err, setErr] = useState<string | null>(null);        // ADDED

  useEffect(() => {                                           // ADDED
    let cancelled = false;                                    // ADDED
    (async () => {
      try {
        const data = await UserAPI.me();
        if (!cancelled) {
          setMe(data);
          localStorage.setItem("username", data.username);    // ADDED: handy elsewhere
        }
      } catch (e: any) {
        if (!cancelled) {
          setErr(e?.message || "Failed to load user");        // ADDED
          setMe(null);
        }
      } finally {
        if (!cancelled) setLoading(false);                    // ADDED
      }
    })();
    return () => { cancelled = true; };                       // ADDED
  }, []);

  return { me, loading, err };                                // ADDED
}