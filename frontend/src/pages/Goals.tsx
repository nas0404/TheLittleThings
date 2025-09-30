// src/pages/GoalsPage.tsx
import React, { useEffect, useMemo, useState } from "react";
import ConfirmDialog from "../components/ui/ConfirmDialog";
import GoalCard from "../components/goals/GoalCard";
import EditGoalModal, { type UpdateGoalRequest } from "../components/goals/EditGoalModal";
import { mapServerErrors } from "../lib/mapServerErrors";
/* ---------- Types (same shapes you had) ---------- */
type Priority = "HIGH" | "MEDIUM" | "LOW";

export interface Category {
  categoryId: number;
  userId: number;
  name: string;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
}
export interface Goal {
  goalId: number;
  userId: number;
  categoryId: number;
  title: string;
  description: string | null;
  priority: Priority;
  createdAt: string;
  updatedAt: string;
}
export interface CreateGoalRequest {
  categoryId?: number;
  title?: string;
  description?: string | null;
  priority?: Priority;
}

/* ---------- API helpers (unchanged) ---------- */
const baseUrl = "/api";

async function parseJsonSafe(res: Response) {
  try { return await res.json(); } catch { return null; }
}

const CategoriesAPI = {
  async list(userId: number): Promise<Category[]> {
    const res = await fetch(`${baseUrl}/users/${userId}/categories`);
    if (!res.ok) throw new Error("failed to load categories");
    return res.json();
  },
};

const GoalsAPI = {
  async list(userId: number, opts?: { categoryId?: number }): Promise<Goal[]> {
    const u = new URL(`${baseUrl}/users/${userId}/goals`, window.location.href);
    if (opts?.categoryId != null) u.searchParams.set("categoryId", String(opts.categoryId));
    const res = await fetch(u.toString());
    if (!res.ok) throw new Error("failed to load goals");
    return res.json();
  },
  async create(userId: number, body: CreateGoalRequest): Promise<Goal> {
    const res = await fetch(`${baseUrl}/users/${userId}/goals`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      const details = await parseJsonSafe(res);
      const err = new Error(details?.message || "Create failed") as any;
      err.status = res.status;
      err.details = details;
      throw err;
    }
    return res.json();
  },
  async update(userId: number, goalId: number, body: UpdateGoalRequest): Promise<Goal> {
    const res = await fetch(`${baseUrl}/users/${userId}/goals/${goalId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      const details = await parseJsonSafe(res);
      const err = new Error(details?.message || "Update failed") as any;
      err.status = res.status;
      err.details = details;
      throw err;
    }
    return res.json();
  },
  async remove(userId: number, goalId: number): Promise<void> {
    const res = await fetch(`${baseUrl}/users/${userId}/goals/${goalId}`, { method: "DELETE" });
    if (!res.ok) {
      const details = await parseJsonSafe(res);
      const err = new Error(details?.message || "Delete failed") as any;
      err.status = res.status;
      err.details = details;
      throw err;
    }
  },
};

/* ---------- Page ---------- */
export default function GoalsPage() {
  const userId = Number(localStorage.getItem("devUserId") ?? 35);

  const [categories, setCategories] = useState<Category[]>([]);
  const [goals, setGoals] = useState<Goal[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // filter (left)
  const [filterCategoryIdStr, setFilterCategoryIdStr] = useState<string>("");

  // create (right)
  const [newCategoryIdStr, setNewCategoryIdStr] = useState<string>("");
  const [newTitle, setNewTitle] = useState("");
  const [newDescription, setNewDescription] = useState("");
  const [newPriority, setNewPriority] = useState<Priority>("MEDIUM");
  const [createErrors, setCreateErrors] = useState<{ [k: string]: string }>({});
  const [creating, setCreating] = useState(false);

  // edit modal
  const [editingGoal, setEditingGoal] = useState<Goal | null>(null);

  // in-app delete confirm
  const [toDelete, setToDelete] = useState<Goal | null>(null);
  const [deleting, setDeleting] = useState(false);

  const selectedFilterCategoryId = useMemo(
    () => (filterCategoryIdStr === "" ? undefined : Number(filterCategoryIdStr)),
    [filterCategoryIdStr]
  );

  async function refreshAll() {
    setLoading(true); setError(null);
    try {
      const [cats, goalsList] = await Promise.all([
        CategoriesAPI.list(userId),
        GoalsAPI.list(userId, { categoryId: selectedFilterCategoryId }),
      ]);
      setCategories(cats);
      setGoals(goalsList);
    } catch (e: any) {
      setError(e?.message ?? "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { refreshAll(); /* eslint-disable-next-line */ }, [userId, selectedFilterCategoryId]);

  // CREATE
  async function handleCreateGoal(e: React.FormEvent) {
    e.preventDefault();
    setCreating(true);
    setCreateErrors({});

    const payload: CreateGoalRequest = {
      priority: newPriority,
      title: newTitle,
      description: newDescription ? newDescription : null,
    };
    if (newCategoryIdStr) payload.categoryId = Number(newCategoryIdStr);

    try {
      await GoalsAPI.create(userId, payload);
      setNewCategoryIdStr(""); setNewTitle(""); setNewDescription(""); setNewPriority("MEDIUM");
      await refreshAll();
    } catch (e: any) {
      const mapped = mapServerErrors(e?.details);
      setCreateErrors(Object.keys(mapped).length ? mapped : { form: e?.message || "Create failed" });
    } finally {
      setCreating(false);
    }
  }

  // UPDATE (called by modal)
  async function handleUpdateGoal(goalId: number, body: UpdateGoalRequest) {
    await GoalsAPI.update(userId, goalId, body);
    await refreshAll();
  }

  // DELETE (in-app confirm)
  function requestDelete(goal: Goal) { setToDelete(goal); }
  async function confirmDelete() {
    if (!toDelete) return;
    setDeleting(true);
    try {
      await GoalsAPI.remove(userId, toDelete.goalId);
      setToDelete(null);
      await refreshAll();
    } catch (e: any) {
      alert(e?.message ?? "Delete failed");
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-8 grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Left: list & filter */}
      <div className="rounded-2xl border border-slate-200 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xl font-semibold">Goals</h2>
          <span className="text-xs text-slate-500">dev user: {userId}</span>
        </div>

        <div className="mb-4">
          <label className="block text-sm font-medium mb-1">Filter by category</label>
          <select
            className="w-40 rounded-lg border px-3 py-2"
            value={filterCategoryIdStr}
            onChange={(e) => setFilterCategoryIdStr(e.target.value)}
          >
            <option value="">All</option>
            {categories.map((c) => (
              <option key={c.categoryId} value={String(c.categoryId)}>
                {c.name}
              </option>
            ))}
          </select>
        </div>

        {loading && <div className="text-sm text-slate-500">Loading…</div>}
        {error && <div className="text-sm text-red-600">{error}</div>}

        {!loading && !error && goals.length === 0 && (
          <div className="text-sm text-slate-500">No goals yet. Add one on the right.</div>
        )}

        <div className="space-y-3">
          {goals.map((g) => (
            <GoalCard
              key={g.goalId}
              goal={g}
              onRequestDelete={() => requestDelete(g)}
              onEdit={() => setEditingGoal(g)}
            />
          ))}
        </div>
      </div>

      {/* Right: New Goal */}
      <div className="rounded-2xl border border-slate-200 p-5">
        <h2 className="text-xl font-semibold mb-3">New Goal</h2>

        {createErrors.form && <div className="mb-3 text-sm text-red-600">{createErrors.form}</div>}

        <form onSubmit={handleCreateGoal} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Category</label>
            <select
              className={`w-full rounded-lg border px-3 py-2 ${createErrors.categoryId ? "border-red-400" : ""}`}
              value={newCategoryIdStr}
              onChange={(e) => setNewCategoryIdStr(e.target.value)}
            >
              <option value="" disabled>Select…</option>
              {categories.map((c) => (
                <option key={c.categoryId} value={String(c.categoryId)}>
                  {c.name}
                </option>
              ))}
            </select>
            {createErrors.categoryId && (
              <p className="text-xs text-red-600 mt-1">{createErrors.categoryId}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Title</label>
            <input
              className={`w-full rounded-lg border px-3 py-2 ${createErrors.title ? "border-red-400" : ""}`}
              placeholder="e.g., Run 5km"
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
            />
            {createErrors.title && (
              <p className="text-xs text-red-600 mt-1">{createErrors.title}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Description</label>
            <textarea
              className="w-full rounded-lg border px-3 py-2"
              placeholder="Optional"
              rows={4}
              value={newDescription}
              onChange={(e) => setNewDescription(e.target.value)}
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Priority</label>
            <select
              className="w-full rounded-lg border px-3 py-2"
              value={newPriority}
              onChange={(e) => setNewPriority(e.target.value as Priority)}
            >
              <option value="HIGH">HIGH</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="LOW">LOW</option>
            </select>
          </div>

          <button
            type="submit"
            disabled={creating}
            className={`w-full rounded-xl bg-black text-white py-2 font-medium hover:opacity-90 ${
              creating ? "opacity-70 cursor-not-allowed" : ""
            }`}
          >
            {creating ? "Saving…" : "Create"}
          </button>
        </form>
      </div>

      {/* Edit modal */}
      {editingGoal && (
        <EditGoalModal
          goal={editingGoal}
          categories={categories}
          onClose={() => setEditingGoal(null)}
          onSave={async (payload: any) => {
            try {
              await handleUpdateGoal(editingGoal.goalId, payload);
              setEditingGoal(null);
            } catch (e) {
              throw e; // handled inside modal
            }
          }}
        />
      )}

      {/* In-app confirm dialog for delete */}
      <ConfirmDialog
        open={!!toDelete}
        title="Delete goal"
        message={toDelete ? `Delete “${toDelete.title}”? This cannot be undone.` : ""}
        confirmText={deleting ? "Deleting…" : "Delete"}
        onConfirm={deleting ? () => {} : confirmDelete}
        onCancel={() => (deleting ? null : setToDelete(null))}
      />
    </div>
  );
}
