"use client";

import { useEffect, useState } from "react";

type ProfileDto = {
  displayName: string;
  bio?: string | null;
  avatarUrl?: string | null;
};

export default function Settings() {
  const [form, setForm] = useState<ProfileDto>({ displayName: "", bio: "", avatarUrl: "" });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  // Load profile on mount
  useEffect(() => {
    (async () => {
      try {
        const res = await fetch("http://localhost:8080/api/settings/profile", {
          headers: { "X-User-Id": "1" },
        });
        if (!res.ok) throw new Error(await res.text());
        const data: ProfileDto = await res.json();
        setForm({
          displayName: data.displayName ?? "",
          bio: data.bio ?? "",
          avatarUrl: data.avatarUrl ?? "",
        });
      } catch {
        setErr("Failed to load profile");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  async function onSave(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setMsg(null);
    setErr(null);
    try {
      const res = await fetch("http://localhost:8080/api/settings/profile", {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "X-User-Id": "1",
        },
        body: JSON.stringify(form),
      });
      if (!res.ok) throw new Error(await res.text());
      const updated: ProfileDto = await res.json();
      setForm(updated);
      setMsg("Saved ✓");
    } catch {
      setErr("Please fill in all the forms");
    } finally {
      setSaving(false);
      setTimeout(() => setMsg(null), 2000);
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
    <section className="space-y-6 max-w-xl">
      <h1 className="text-2xl font-bold">Settings</h1>

      {/* Avatar preview */}
      <div className="flex items-center gap-3">
        <div className="h-16 w-16 rounded-full overflow-hidden bg-gray-200">
          {form.avatarUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={form.avatarUrl} alt="avatar" className="h-full w-full object-cover" />
          ) : null}
        </div>
        <span className="text-sm text-gray-500">Avatar preview</span>
      </div>

      <form className="space-y-3" onSubmit={onSave}>
        <label className="block">
          <span className="text-sm">Avatar URL</span>
          <input
            className="mt-1 w-full rounded-lg border p-2"
            placeholder="https://…"
            value={form.avatarUrl ?? ""}
            onChange={(e) => setForm((f) => ({ ...f, avatarUrl: e.target.value }))}
          />
        </label>

        <label className="block">
          <span className="text-sm">Display name *</span>
          <input
            className="mt-1 w-full rounded-lg border p-2"
            placeholder="Your name"
            required
            value={form.displayName}
            onChange={(e) => setForm((f) => ({ ...f, displayName: e.target.value }))}
          />
        </label>

        <label className="block">
          <span className="text-sm">Bio</span>
          <textarea
            className="mt-1 w-full rounded-lg border p-2"
            rows={4}
            placeholder="Tell us about yourself…"
            value={form.bio ?? ""}
            onChange={(e) => setForm((f) => ({ ...f, bio: e.target.value }))}
          />
        </label>

        <div className="flex items-center gap-3">
          <button className="rounded-xl bg-black px-4 py-2 text-white disabled:opacity-60" disabled={saving}>
            {saving ? "Saving…" : "Save"}
          </button>
          {msg && <span className="text-green-600 text-sm">{msg}</span>}
          {err && <span className="text-red-600 text-sm">{err}</span>}
        </div>
      </form>
      {/* Notifications — quick demo */}
      
<div className="mt-8 space-y-2">
  <h2 className="text-xl font-semibold">Notifications</h2>
  <button
    onClick={async () => {
      try {
        const res = await fetch("http://localhost:8080/api/settings/notifications/reset", {
          method: "POST",
          headers: { "X-User-Id": "1" },
        });
        const data = await res.json();
        alert("Notifications reset to defaults:\n" + JSON.stringify(data, null, 2));
      } catch {
        alert("Reset failed");
      }
    }}
    className="rounded-xl bg-black px-4 py-2 text-white"
  >
    Reset to defaults
  </button>
</div>

    </section>
  );
}
