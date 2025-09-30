// src/pages/GoalsPage.tsx
import React, { useEffect, useMemo, useState } from "react";
import ConfirmDialog from "../components/ui/ConfirmDialog";
import GoalCard from "../components/goals/GoalCard";
import EditGoalModal, { type UpdateGoalRequest } from "../components/goals/EditGoalModal";
import { mapServerErrors } from "../lib/mapServerErrors";

// Use your centralized APIs
import { GoalsAPI, type GoalResponse, type Priority, type CreateGoalRequest } from "../api/GoalApi";
import { CategoriesAPI, type Category as UICategory } from "../api/CategoryApi";

// ---- NEW: sort key type ----
type SortKey = "createdAt_desc" | "priority_desc";

export default function GoalsPage() {
  const devUserId = localStorage.getItem("devUserId") ?? "—";

  // data
  const [categories, setCategories] = useState<UICategory[]>([]);
  const [goals, setGoals] = useState<GoalResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // filter (left)
  const [filterCategoryIdStr, setFilterCategoryIdStr] = useState<string>("");

  // ---- NEW: sort state ----
  const [sortBy, setSortBy] = useState<SortKey>("createdAt_desc");

  // create (right)
  const [newCategoryIdStr, setNewCategoryIdStr] = useState<string>("");
  const [newTitle, setNewTitle] = useState("");
  const [newDescription, setNewDescription] = useState("");
  const [newPriority, setNewPriority] = useState<Priority>("MEDIUM");
  const [createErrors, setCreateErrors] = useState<Record<string, string>>({});
  const [creating, setCreating] = useState(false);

  // edit / delete
  const [editingGoal, setEditingGoal] = useState<GoalResponse | null>(null);
  const [toDelete, setToDelete] = useState<GoalResponse | null>(null);
  const [deleting, setDeleting] = useState(false);

  const selectedFilterCategoryId = useMemo(
    () => (filterCategoryIdStr === "" ? undefined : Number(filterCategoryIdStr)),
    [filterCategoryIdStr]
  );

  async function refreshAll() {
    setLoading(true);
    setError(null);
    try {
      const [cats, goalsList] = await Promise.all([
        CategoriesAPI.list(), // UI categories { id, name, ... }
        GoalsAPI.list({ categoryId: selectedFilterCategoryId }),
      ]);
      setCategories(cats);
      setGoals(goalsList);
    } catch (e: any) {
      setError(e?.message ?? "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refreshAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedFilterCategoryId]);

  // ---- NEW: compute sorted list per AC ----
  const sortedGoals = useMemo(() => {
    const byNewest = (a: GoalResponse, b: GoalResponse) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();

    if (sortBy === "createdAt_desc") return [...goals].sort(byNewest);

    const weight: Record<Priority, number> = { HIGH: 0, MEDIUM: 1, LOW: 2 };
    return [...goals].sort((a, b) => {
      const wa = weight[a.priority] ?? 3;
      const wb = weight[b.priority] ?? 3;
      if (wa !== wb) return wa - wb; // HIGH→MEDIUM→LOW
      return byNewest(a, b);         // tie-break by newest
    });
  }, [goals, sortBy]);

  // CREATE
  async function handleCreateGoal(e: React.FormEvent) {
    e.preventDefault();
    setCreating(true);
    setCreateErrors({});

    const payload: Required<CreateGoalRequest> = {
      categoryId: Number(newCategoryIdStr),                // required by backend
      title: newTitle.trim(),
      description: newDescription ? newDescription : null,
      priority: newPriority,
    };

    try {
      await GoalsAPI.create(payload);
      setNewCategoryIdStr("");
      setNewTitle("");
      setNewDescription("");
      setNewPriority("MEDIUM");
      await refreshAll();
    } catch (e: any) {
      const mapped = mapServerErrors(e?.details);
      setCreateErrors(Object.keys(mapped).length ? mapped : { form: e?.message || "Create failed" });
    } finally {
      setCreating(false);
    }
  }

  // UPDATE (modal)
  async function handleUpdateGoal(goalId: number, body: UpdateGoalRequest) {
    await GoalsAPI.update(goalId, body);
    await refreshAll();
  }

  // DELETE
  function requestDelete(goal: GoalResponse) {
    setToDelete(goal);
  }
  async function confirmDelete() {
    if (!toDelete) return;
    setDeleting(true);
    try {
      await GoalsAPI.remove(toDelete.goalId);
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
      {/* Left: list, filter, sort */}
      <div className="rounded-2xl border border-slate-200 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xl font-semibold">Goals</h2>
          <span className="text-xs text-slate-500">dev user: {devUserId}</span>
        </div>

        {/* Filter + Sort row */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
          <div>
            <label className="block text-sm font-medium mb-1">Filter by category</label>
            <select
              className="w-full rounded-lg border px-3 py-2"
              value={filterCategoryIdStr}
              onChange={(e) => setFilterCategoryIdStr(e.target.value)}
            >
              <option value="">All</option>
              {categories.map((c) => (
                <option key={c.id} value={String(c.id)}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          {/* NEW: Sort control */}
          <div>
            <label className="block text-sm font-medium mb-1">Sort by</label>
            <select
              className="w-full rounded-lg border px-3 py-2"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value as SortKey)}
            >
              <option value="createdAt_desc">Newest first</option>
              <option value="priority_desc">Priority (HIGH → LOW)</option>
            </select>
          </div>
        </div>

        {loading && <div className="text-sm text-slate-500">Loading…</div>}
        {error && <div className="text-sm text-red-600">{error}</div>}

        {!loading && !error && sortedGoals.length === 0 && (
          <div className="text-sm text-slate-500">No goals yet. Add one on the right.</div>
        )}

        <div className="space-y-3">
          {sortedGoals.map((g) => (
            <GoalCard
              key={g.goalId}
              goal={{
                goalId: g.goalId,
                title: g.title,
                description: g.description ?? null, // normalize for GoalCard type
                priority: g.priority,
              }}
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
              <option value="" disabled>
                Select…
              </option>
              {categories.map((c) => (
                <option key={c.id} value={String(c.id)}>
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
            {createErrors.title && <p className="text-xs text-red-600 mt-1">{createErrors.title}</p>}
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
          goal={{
            goalId: editingGoal.goalId,
            title: editingGoal.title,
            description: editingGoal.description ?? null,
            priority: editingGoal.priority,
            categoryId: editingGoal.categoryId,
          }}
          categories={categories.map((c) => ({ categoryId: c.id, name: c.name }))}
          onClose={() => setEditingGoal(null)}
          onSave={async (payload: UpdateGoalRequest) => {
            try {
              await handleUpdateGoal(editingGoal.goalId, payload);
              setEditingGoal(null);
            } catch (e) {
              throw e; // handled inside modal
            }
          }}
        />
      )}

      {/* Delete confirm */}
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
