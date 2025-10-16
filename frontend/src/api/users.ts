import { useEffect, useState } from "react";
import { http } from "./http";

export type MeResponse = {
  userId: number;
  username: string;
  streaks?: number | null;
  trophies?: number | null;
};

export type LoginRequest = {
  username: string;
  password: string;
};

export type RegisterRequest = {
  username: string;
  email: string;
  password: string;
};

export type LoginResponse = {
  token: string;
  user: MeResponse;
};

export type UpdateUserRequest = {
  username?: string;
  email?: string;
  password?: string;
};

// Authentication and user management API
export const UserAPI = {
  // Get current user info
  async me(): Promise<MeResponse> {
    return http<MeResponse>(`/api/users/me`);
  },

  // Login user
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    return http<LoginResponse>(`/api/auth/login`, {
      method: "POST",
      body: JSON.stringify(credentials),
    });
  },

  // Register new user
  async register(userData: RegisterRequest): Promise<LoginResponse> {
    return http<LoginResponse>(`/api/auth/register`, {
      method: "POST",
      body: JSON.stringify(userData),
    });
  },

  // Update user profile
  async update(userId: number, userData: UpdateUserRequest): Promise<MeResponse> {
    return http<MeResponse>(`/api/users/${userId}`, {
      method: "PUT",
      body: JSON.stringify(userData),
    });
  },

  // Logout (client-side token removal)
  logout(): void {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
  },
};

// Legacy function for backward compatibility
export async function fetchMe(): Promise<MeResponse> {
  return UserAPI.me();
}


export function useMe() {
  const [me, setMe] = useState<MeResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const data = await fetchMe();
        setMe(data);
        localStorage.setItem("username", data.username); // handy for other components
      } catch (e: any) {
        setErr(e.message || "Failed to load user");
        setMe(null);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return { me, loading, err };
}
