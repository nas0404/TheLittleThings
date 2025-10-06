import { useEffect, useState } from "react";

export type MeResponse = {
  userId: number;
  username: string;
  streaks?: number | null;
  trophies?: number | null;
};

const BASE = "http://localhost:8080";

export async function fetchMe(): Promise<MeResponse> {
  const r = await fetch(`${BASE}/api/users/me`, {
    headers: { Authorization: `Bearer ${localStorage.getItem("token") || ""}` },
  });
  if (!r.ok) throw new Error("Not logged in");
  return r.json();
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
