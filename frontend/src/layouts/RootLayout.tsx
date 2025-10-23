import React, { useEffect, useState, useCallback } from "react";
import { NavLink, useLocation } from "react-router-dom";

type Props = { children: React.ReactNode };

type MeResponse = {
  userId: number;
  username: string;
  email: string;
};

type ProfileResponse = {
  displayName: string;
  bio: string | null;
  avatarUrl: string | null;
};

export default function RootLayout({ children }: Props) {
  const [me, setMe] = useState<MeResponse | null>(null);
  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const location = useLocation();

  // Hide Navbar on signin/register pages
  const hideNavbarRoutes = ["/", "/register", "/login"];
  const shouldHideNavbar = hideNavbarRoutes.includes(location.pathname);

  const fetchAll = useCallback(async () => {
    const token = localStorage.getItem("token");
    if (!token) {
      setMe(null);
      setProfile(null);
      return;
    }

    try {
      // 1) who am I? (server-relative path)
      const meRes = await fetch("/api/users/me", {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!meRes.ok) throw new Error("Not logged in");

      const meJson: MeResponse = await meRes.json();
      setMe(meJson);
      localStorage.setItem("username", meJson.username);
      localStorage.setItem("userId", String(meJson.userId));

      // 2) profile (server-relative path)
      const profRes = await fetch("/api/settings/profile", {
        headers: {
          Authorization: `Bearer ${token}`,
          "X-User-Id": String(meJson.userId),
          "Content-Type": "application/json",
        },
      });

      if (profRes.ok) {
        const profJson: ProfileResponse = await profRes.json();
        setProfile(profJson);
      } else {
        setProfile(null);
      }
    } catch {
      setMe(null);
      setProfile(null);
    }
  }, []);

  useEffect(() => {
    fetchAll();

    // refresh navbar when settings page saves or token changes
    const onUserUpdated = () => fetchAll();
    const onStorage = (e: StorageEvent) => {
      if (e.key === "token") fetchAll();
    };
    window.addEventListener("user-updated", onUserUpdated as EventListener);
    window.addEventListener("storage", onStorage);
    return () => {
      window.removeEventListener("user-updated", onUserUpdated as EventListener);
      window.removeEventListener("storage", onStorage);
    };
  }, [fetchAll]);

  // avatar initials fallback
  const initials =
    (me?.username ?? "")
      .split(/\s+/)
      .filter(Boolean)
      .map((s) => s[0]?.toUpperCase() ?? "")
      .join("")
      .slice(0, 2) || "?";

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      {!shouldHideNavbar && (
        <header className="border-b bg-white">
          <nav className="mx-auto flex max-w-6xl items-center gap-4 p-4">
            <NavLink to="/" className="font-semibold">
              TheLittleThing
            </NavLink>
            <div className="ml-auto flex items-center gap-3">
              <NavLink to="/home" className="hover:underline">Home</NavLink>
              <NavLink to="/about" className="hover:underline">About</NavLink>
              <NavLink to="/settings" className="hover:underline">Settings</NavLink>
              <NavLink to="/categories" className="hover:underline">Categories</NavLink>
              <NavLink to="/goals" className="hover:underline">Goals</NavLink>
              <NavLink to="/wins" className="hover:underline">Wins</NavLink>
              <NavLink to="/journal" className="hover:underline">Journal</NavLink>
              <NavLink to="/leaderboard" className="hover:underline">Leaderboard</NavLink>
              <NavLink to="/user" className="hover:underline">Account</NavLink>
              <NavLink to="/friends" className="hover:underline">Friends</NavLink>
              <NavLink to="/creategoals" className="hover:underline">Add Goal</NavLink>

              {me && (
                <span className="ml-4 inline-flex items-center gap-2 rounded-full bg-gray-100 px-3 py-1 text-sm font-medium text-gray-700">
                  {profile?.avatarUrl ? (
                    <img
                      src={profile.avatarUrl}
                      alt={profile.displayName ?? me.username}
                      className="h-6 w-6 rounded-full object-cover"
                    />
                  ) : (
                    <span className="flex h-6 w-6 items-center justify-center rounded-full bg-gray-200 text-xs font-semibold">
                      {initials}
                    </span>
                  )}
                  <span>{profile?.displayName ?? me.username}</span>
                </span>
              )}
            </div>
          </nav>
        </header>
      )}

      <main className="mx-auto max-w-6xl p-6">{children}</main>

      <footer className="mx-auto max-w-6xl p-6 text-sm text-gray-500">
        © {new Date().getFullYear()} TheLittleThing
      </footer>
    </div>
  );
}
