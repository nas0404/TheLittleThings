import { NavLink, useLocation } from "react-router-dom";
import React, { useEffect, useState } from "react";

type Props = { children: React.ReactNode };

type MeResponse = {
  userId: number;
  username: string;
  email: string;
  // add other fields you return from /me
};

export default function RootLayout({ children }: Props) {
  const [me, setMe] = useState<MeResponse | null>(null);
  const location = useLocation();
  
  // Hide Navbar on signin/register pages
  const hideNavbarRoutes = ['/', '/register', '/login'];
  const shouldHideNavbar = hideNavbarRoutes.includes(location.pathname);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return;

    fetch("/api/users/me", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => {
        if (!res.ok) throw new Error("Not logged in");
        return res.json();
      })
      .then((data) => setMe(data))
      .catch(() => setMe(null));
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      {!shouldHideNavbar && (
        <header className="border-b bg-white">
          <nav className="mx-auto flex max-w-6xl items-center gap-4 p-4">
            <NavLink to="/" className="font-semibold">
              TheLittleThing
            </NavLink>
            <div className="ml-auto flex items-center gap-3">
              <NavLink to="/home" className="hover:underline">
                Home
              </NavLink>
              <NavLink to="/about" className="hover:underline">
                About
              </NavLink>
              <NavLink to="/settings" className="hover:underline">
                Settings
              </NavLink>
              <NavLink to="/categories" className="hover:underline">
                Categories
              </NavLink>
              <NavLink to="/goals" className="hover:underline">
                Goals
              </NavLink>
              <NavLink to="/wins" className="hover:underline">
                Wins
              </NavLink>
              <NavLink to="/journal" className="hover:underline">
                Journal
              </NavLink>
              <NavLink to="/leaderboard" className="hover:underline">
                Leaderboard
              </NavLink>
              <NavLink to="/user" className="hover:underline">
                Account
              </NavLink>
              <NavLink to="/friends" className="hover:underline">
                Friends
              </NavLink>

              {/* username display */}
              {me && (
                <span className="ml-4 rounded-full bg-gray-100 px-3 py-1 text-sm font-medium text-gray-700">
                  {me.username}
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
