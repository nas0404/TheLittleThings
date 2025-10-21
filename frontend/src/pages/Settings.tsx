import { useEffect, useState } from "react";

/** ---------------- Types ---------------- */
type ProfileDto = {
  displayName: string;
  bio?: string | null;
  avatarUrl?: string | null;
};

type NotificationPrefsDto = {
  winDailyReminder: boolean;
  streakMilestones: boolean;
  trophies: boolean;
  weeklyChallenges: boolean;
  friendRequests: boolean;
  channelInApp: boolean;
  channelEmail: boolean;
};

const DEFAULT_PREFS: NotificationPrefsDto = {
  winDailyReminder: true,
  streakMilestones: true,
  trophies: true,
  weeklyChallenges: true,
  friendRequests: true,
  channelInApp: true,
  channelEmail: false,
};

const API = "http://localhost:8080"; // hard-coded for demo
const DEMO_HEADERS = { "X-User-Id": "1" };

/** Small helper for JSON fetch with common headers + errors */
async function fetchJSON<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    ...init,
    headers: { ...(init?.headers || {}), ...DEMO_HEADERS },
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `${res.status} ${res.statusText}`);
  }
  return (res.status === 204 ? (undefined as T) : res.json());
}

/** ---------------- Page ---------------- */
export default function Settings() {
  // Profile (#79)
  const [profile, setProfile] = useState<ProfileDto>({
    displayName: "",
    bio: "",
    avatarUrl: "",
  });
  const [savingProfile, setSavingProfile] = useState(false);
  const [profileMsg, setProfileMsg] = useState<string | null>(null);
  const [profileErr, setProfileErr] = useState<string | null>(null);

  // Notifications (#76, #77, #82)
  const [prefs, setPrefs] = useState<NotificationPrefsDto>(DEFAULT_PREFS);
  const [prefsLoaded, setPrefsLoaded] = useState(false);
  const [savingPrefs, setSavingPrefs] = useState(false);
  const [prefsMsg, setPrefsMsg] = useState<string | null>(null);
  const [prefsErr, setPrefsErr] = useState<string | null>(null);

  // Security (#81)
  const [currentPasswordU, setCurrentPasswordU] = useState("");
  const [newUsername, setNewUsername] = useState("");
  const [savingUsername, setSavingUsername] = useState(false);
  const [userMsg, setUserMsg] = useState<string | null>(null);
  const [userErr, setUserErr] = useState<string | null>(null);

  const [currentPasswordP, setCurrentPasswordP] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [savingPassword, setSavingPassword] = useState(false);
  const [passMsg, setPassMsg] = useState<string | null>(null);
  const [passErr, setPassErr] = useState<string | null>(null);

  // Account control (#83)
  const [deactivateMsg, setDeactivateMsg] = useState<string | null>(null);
  const [deactivateErr, setDeactivateErr] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState("");

  const [loading, setLoading] = useState(true);

  /** Load profile + prefs on mount */
  useEffect(() => {
    (async () => {
      try {
        const [p, n] = await Promise.all([
          fetchJSON<ProfileDto>(`${API}http://localhost:8080/api/profile`),
          //fetchJSON("http://localhost:8080/api/profile")
          fetchJSON<NotificationPrefsDto>(`${API}/api/settings/notifications`),
        ]);
        setProfile({
          displayName: p.displayName ?? "",
          bio: p.bio ?? "",
          avatarUrl: p.avatarUrl ?? "",
        });
        setPrefs({ ...DEFAULT_PREFS, ...n });
      } catch (e) {
        // Non-fatal for demo; keep defaults
        console.warn("Initial load error:", e);
      } finally {
        setPrefsLoaded(true);
        setLoading(false);
      }
    })();
  }, []);

  /** -------- Profile handlers (#79) -------- */
  async function saveProfile(e: React.FormEvent) {
    e.preventDefault();
    if (!profile.displayName.trim()) {
      setProfileErr("Username is required");
      return;
    }
    setSavingProfile(true);
    setProfileMsg(null);
    setProfileErr(null);
    try {
      const updated = await fetchJSON<ProfileDto>(`${API}/api/settings/profile`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(profile),
      });
      setProfile(updated);
      setProfileMsg("Profile saved ✓");
    } catch (e: any) {
      console.error("Save profile failed:", e.message);
      setProfileErr("Save failed");
    } finally {
      setSavingProfile(false);
      setTimeout(() => setProfileMsg(null), 2000);
    }
  }

  /** -------- Notifications handlers (#76, #77, #82) -------- */
  async function savePrefs() {
    setSavingPrefs(true);
    setPrefsMsg(null);
    setPrefsErr(null);
    try {
      const updated = await fetchJSON<NotificationPrefsDto>(`${API}/api/settings/notifications`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(prefs),
      });
      setPrefs({ ...DEFAULT_PREFS, ...updated });
      setPrefsMsg("Preferences saved ✓");
    } catch (e: any) {
      console.error("Save prefs failed:", e.message);
      setPrefsErr("Save failed");
    } finally {
      setSavingPrefs(false);
      setTimeout(() => setPrefsMsg(null), 2000);
    }
  }

  async function resetPrefs() {
    setSavingPrefs(true);
    setPrefsMsg(null);
    setPrefsErr(null);
    try {
      const defaults = await fetchJSON<NotificationPrefsDto>(
        `${API}/api/settings/notifications/reset`,
        { method: "POST" }
      );
      setPrefs({ ...DEFAULT_PREFS, ...defaults });
      setPrefsMsg("Preferences reset ✓");
    } catch (e: any) {
      console.error("Reset prefs failed:", e.message);
      setPrefsErr("Reset failed");
    } finally {
      setSavingPrefs(false);
      setTimeout(() => setPrefsMsg(null), 2000);
    }
  }

  /** -------- Security handlers (#81) -------- */
  async function changeUsername(e: React.FormEvent) {
    e.preventDefault();
    if (!currentPasswordU || !newUsername.trim()) {
      setUserErr("Enter current password and a new username.");
      return;
    }
    setSavingUsername(true);
    setUserMsg(null);
    setUserErr(null);
    try {
      await fetchJSON<void>(`${API}/api/settings/username`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ currentPassword: currentPasswordU, newUsername }),
      });
      setUserMsg("Username changed ✓");
      setCurrentPasswordU("");
      setNewUsername("");
    } catch (e: any) {
      console.error("Change username failed:", e.message);
      setUserErr("Change failed");
    } finally {
      setSavingUsername(false);
      setTimeout(() => setUserMsg(null), 2500);
    }
  }

  async function changePassword(e: React.FormEvent) {
    e.preventDefault();
    if (!currentPasswordP || !newPassword) {
      setPassErr("Enter current and new password.");
      return;
    }
    if (newPassword.length < 8) {
      setPassErr("New password must be at least 8 characters.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setPassErr("Passwords do not match.");
      return;
    }
    setSavingPassword(true);
    setPassMsg(null);
    setPassErr(null);
    try {
      await fetchJSON<void>(`${API}/api/settings/password`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ currentPassword: currentPasswordP, newPassword }),
      });
      setPassMsg("Password changed ✓");
      setCurrentPasswordP("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (e: any) {
      console.error("Change password failed:", e.message);
      setPassErr("Change failed");
    } finally {
      setSavingPassword(false);
      setTimeout(() => setPassMsg(null), 2500);
    }
  }

  /** -------- Account control handlers (#83) -------- */
  async function deactivateAccount() {
    setDeactivateErr(null);
    setDeactivateMsg(null);
    try {
      await fetchJSON<void>(`${API}/api/settings/deactivate`, { method: "POST" });
      setDeactivateMsg("Account deactivated ✓");
    } catch (e: any) {
      console.error("Deactivate failed:", e.message);
      setDeactivateErr("Failed to deactivate");
    }
  }

  async function deleteAccount() {
    if (deleteConfirm !== "DELETE") {
      setDeactivateErr('Type DELETE to confirm.');
      return;
    }
    setDeleting(true);
    setDeactivateErr(null);
    setDeactivateMsg(null);
    try {
      await fetchJSON<void>(`${API}/api/settings/account`, { method: "DELETE" });
      setDeactivateMsg("Account deleted ✓");
      // In a real app you would redirect to a goodbye page or sign-out.
    } catch (e: any) {
      console.error("Delete failed:", e.message);
      setDeactivateErr("Failed to delete");
    } finally {
      setDeleting(false);
    }
  }

  if (loading) {
    return (
      <section className="space-y-4">
        <h1 className="text-2xl font-bold">Settings</h1>
        <p>Loading…</p>
      </section>
    );
  }

  return (
    <section className="space-y-10 max-w-2xl">
      <h1 className="text-2xl font-bold">Settings</h1>

      {/* ===== Profile (#79) ===== */}
      <div className="space-y-3">
        <h2 className="text-xl font-semibold">Profile</h2>
        <form className="space-y-3" onSubmit={saveProfile}>
          <div className="flex items-center gap-3">
            <div className="h-16 w-16 rounded-full overflow-hidden bg-gray-200">
              {profile.avatarUrl ? (
                // eslint-disable-next-line jsx-a11y/alt-text
                <img src={profile.avatarUrl} className="h-full w-full object-cover" />
              ) : null}
            </div>
            <span className="text-sm text-gray-500">Avatar preview</span>
          </div>

          <label className="block">
            <span className="text-sm">Avatar URL</span>
            <input
              className="mt-1 w-full rounded-lg border p-2"
              placeholder="https://…"
              value={profile.avatarUrl ?? ""}
              onChange={(e) => setProfile((f) => ({ ...f, avatarUrl: e.target.value }))}
            />
          </label>

          <label className="block">
            <span className="text-sm">Username *</span>
            <input
              className="mt-1 w-full rounded-lg border p-2"
              placeholder="Update your username"
              required
              value={profile.displayName}
              onChange={(e) => setProfile((f) => ({ ...f, displayName: e.target.value }))}
            />
          </label>

          <label className="block">
            <span className="text-sm">Bio</span>
            <textarea
              className="mt-1 w-full rounded-lg border p-2"
              rows={4}
              placeholder="Tell us about yourself…"
              value={profile.bio ?? ""}
              onChange={(e) => setProfile((f) => ({ ...f, bio: e.target.value }))}
            />
          </label>

          <div className="flex items-center gap-3">
            <button className="rounded-xl bg-black px-4 py-2 text-white disabled:opacity-60" disabled={savingProfile}>
              {savingProfile ? "Saving…" : "Save profile"}
            </button>
            {profileMsg && <span className="text-green-600 text-sm">{profileMsg}</span>}
            {profileErr && <span className="text-red-600 text-sm">{profileErr}</span>}
          </div>
        </form>
      </div>

      {/* ===== Notifications (#76, #77, #82) ===== */}
      <div className="space-y-3">
        <h2 className="text-xl font-semibold">Notifications</h2>

        <div className="rounded-lg border p-4 space-y-4">
          {/* Which notifications (#76) */}
          <div className="space-y-2">
            <h3 className="font-medium">Which notifications</h3>
            {[
              ["winDailyReminder", "Daily win reminders"],
              ["streakMilestones", "Streak milestones"],
              ["trophies", "Trophies"],
              ["weeklyChallenges", "Weekly challenges"],
              ["friendRequests", "Friend requests"],
            ].map(([key, label]) => (
              <div className="flex items-center gap-2" key={key}>
                <input
                  id={key}
                  type="checkbox"
                  checked={(prefs as any)[key]}
                  disabled={!prefsLoaded}
                  onChange={(e) =>
                    setPrefs((p) => ({ ...p, [key]: e.target.checked } as NotificationPrefsDto))
                  }
                />
                <label htmlFor={key}>{label}</label>
              </div>
            ))}
          </div>

          {/* Delivery channels (#77) */}
          <div className="space-y-2">
            <h3 className="font-medium">Delivery</h3>
            <div className="flex items-center gap-2">
              <input
                id="inapp"
                type="checkbox"
                checked={prefs.channelInApp}
                disabled={!prefsLoaded}
                onChange={(e) => setPrefs({ ...prefs, channelInApp: e.target.checked })}
              />
              <label htmlFor="inapp">In-app</label>
            </div>
            <div className="flex items-center gap-2">
              <input
                id="email"
                type="checkbox"
                checked={prefs.channelEmail}
                disabled={!prefsLoaded}
                onChange={(e) => setPrefs({ ...prefs, channelEmail: e.target.checked })}
              />
              <label htmlFor="email">Email</label>
            </div>
          </div>

          {/* Actions (#77, #82) */}
          <div className="flex items-center gap-3 pt-1">
            <button
              type="button"
              onClick={savePrefs}
              className="rounded-xl bg-black px-4 py-2 text-white disabled:opacity-60"
              disabled={savingPrefs || !prefsLoaded}
            >
              {savingPrefs ? "Saving…" : "Save preferences"}
            </button>
            <button
              type="button"
              onClick={resetPrefs}
              className="rounded-xl border px-4 py-2 disabled:opacity-60"
              disabled={savingPrefs || !prefsLoaded}
            >
              Reset to defaults
            </button>
            {prefsMsg && <span className="text-green-600 text-sm">{prefsMsg}</span>}
            {prefsErr && <span className="text-red-600 text-sm">{prefsErr}</span>}
          </div>
        </div>
      </div>

      {/* ===== Security (#81) ===== */}
      <div className="space-y-3">
        <h2 className="text-xl font-semibold">Security</h2>

        {/* Change username */}
        <form className="rounded-lg border p-4 space-y-3" onSubmit={changeUsername}>
          <h3 className="font-medium">Change username</h3>
          <label className="block">
            <span className="text-sm">Current password</span>
            <input
              type="password"
              className="mt-1 w-full rounded-lg border p-2"
              value={currentPasswordU}
              onChange={(e) => setCurrentPasswordU(e.target.value)}
            />
          </label>
          <label className="block">
            <span className="text-sm">New username</span>
            <input
              className="mt-1 w-full rounded-lg border p-2"
              value={newUsername}
              onChange={(e) => setNewUsername(e.target.value)}
            />
          </label>
          <div className="flex items-center gap-3">
            <button className="rounded-xl bg-black px-4 py-2 text-white disabled:opacity-60" disabled={savingUsername}>
              {savingUsername ? "Saving…" : "Save username"}
            </button>
            {userMsg && <span className="text-green-600 text-sm">{userMsg}</span>}
            {userErr && <span className="text-red-600 text-sm">{userErr}</span>}
          </div>
        </form>

        {/* Change password */}
        <form className="rounded-lg border p-4 space-y-3" onSubmit={changePassword}>
          <h3 className="font-medium">Change password</h3>
          <label className="block">
            <span className="text-sm">Current password</span>
            <input
              type="password"
              className="mt-1 w-full rounded-lg border p-2"
              value={currentPasswordP}
              onChange={(e) => setCurrentPasswordP(e.target.value)}
            />
          </label>
          <label className="block">
            <span className="text-sm">New password</span>
            <input
              type="password"
              className="mt-1 w-full rounded-lg border p-2"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
          </label>
          <label className="block">
            <span className="text-sm">Confirm new password</span>
            <input
              type="password"
              className="mt-1 w-full rounded-lg border p-2"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </label>
          <div className="flex items-center gap-3">
            <button className="rounded-xl bg-black px-4 py-2 text-white disabled:opacity-60" disabled={savingPassword}>
              {savingPassword ? "Saving…" : "Save password"}
            </button>
            {passMsg && <span className="text-green-600 text-sm">{passMsg}</span>}
            {passErr && <span className="text-red-600 text-sm">{passErr}</span>}
          </div>
        </form>
      </div>

      {/* ===== Account Control (#83) ===== */}
      <div className="space-y-3">
        <h2 className="text-xl font-semibold text-red-600">Danger zone</h2>

        <div className="rounded-lg border p-4 space-y-3">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={deactivateAccount}
              className="rounded-xl border px-4 py-2"
            >
              Deactivate account
            </button>
            {deactivateMsg && <span className="text-green-600 text-sm">{deactivateMsg}</span>}
            {deactivateErr && <span className="text-red-600 text-sm">{deactivateErr}</span>}
          </div>

          <div className="space-y-2">
            <p className="text-sm">
              Permanently delete your account and all associated data. This action cannot be undone.
            </p>
            <div className="flex items-center gap-2">
              <input
                className="w-full rounded-lg border p-2"
                placeholder='Type "DELETE" to confirm'
                value={deleteConfirm}
                onChange={(e) => setDeleteConfirm(e.target.value)}
              />
              <button
                type="button"
                onClick={deleteAccount}
                disabled={deleting}
                className="rounded-xl bg-red-600 px-4 py-2 text-white disabled:opacity-60"
              >
                {deleting ? "Deleting…" : "Delete account"}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
