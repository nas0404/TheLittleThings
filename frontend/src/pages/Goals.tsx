import React, { useEffect, useMemo, useState } from "react";
import ConfirmDialog from "../components/ui/ConfirmDialog";
import GoalCard from "../components/goals/GoalCard";
import EditGoalModal, { type UpdateGoalRequest } from "../components/goals/EditGoalModal";
import { mapServerErrors } from "../lib/mapServerErrors";

import { GoalsAPI, type GoalResponse, type Priority, type CreateGoalRequest } from "../api/GoalApi";
import { CategoriesAPI, type Category as UICategory } from "../api/CategoryApi";

// Type for sorting options
type SortKey = "createdAt_desc" | "priority_desc";

// Main Goals page component
export default function GoalsPage() {

  // State for data management
  const [categories, setCategories] = useState<UICategory[]>([]);
  const [goals, setGoals] = useState<GoalResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filtering state
  const [filterCategoryIdStr, setFilterCategoryIdStr] = useState<string>("");

  // Sorting state
  const [sortBy, setSortBy] = useState<SortKey>("createdAt_desc");

  // New goal form state
  const [newCategoryIdStr, setNewCategoryIdStr] = useState<string>("");
  const [newTitle, setNewTitle] = useState("");
  const [newDescription, setNewDescription] = useState("");
  const [newPriority, setNewPriority] = useState<Priority>("MEDIUM");
  const [createErrors, setCreateErrors] = useState<Record<string, string>>({});
  const [creating, setCreating] = useState(false);

  // Edit/Delete modal state
  const [editingGoal, setEditingGoal] = useState<GoalResponse | null>(null);
  const [toDelete, setToDelete] = useState<GoalResponse | null>(null);
  const [deleting, setDeleting] = useState(false);

  // Computed value for selected category filter
  const selectedFilterCategoryId = useMemo(
    () => (filterCategoryIdStr === "" ? undefined : Number(filterCategoryIdStr)),
    [filterCategoryIdStr]
  );

  // Fetch all categories and goals data
  async function refreshAll() {
    setLoading(true);
    setError(null);
    try {
      // Parallel API calls for better performance
      const [cats, goalsList] = await Promise.all([
        CategoriesAPI.list(),
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
  }, [selectedFilterCategoryId]);

  // Memoized sorted goals list based on current sort criteria
  const sortedGoals = useMemo(() => {
    // Sort function for newest first
    const byNewest = (a: GoalResponse, b: GoalResponse) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();

    // If sorting by creation date, just sort by newest
    if (sortBy === "createdAt_desc") return [...goals].sort(byNewest);

    // Priority weights for sorting (HIGH = 0, MEDIUM = 1, LOW = 2)
    const weight: Record<Priority, number> = { HIGH: 0, MEDIUM: 1, LOW: 2 };
    return [...goals].sort((a, b) => {
      // Get weights, default to 3 for unknown priorities
      const wa = weight[a.priority] ?? 3;
      const wb = weight[b.priority] ?? 3;
      // Sort by priority first, then by creation date
      if (wa !== wb) return wa - wb;
      return byNewest(a, b);
    });
  }, [goals, sortBy]);

  async function handleCreateGoal(e: React.FormEvent) {
    e.preventDefault();
    setCreating(true);
    setCreateErrors({});

    const payload: Required<CreateGoalRequest> = {
      categoryId: Number(newCategoryIdStr),
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

  async function handleUpdateGoal(goalId: number, body: UpdateGoalRequest) {
    await GoalsAPI.update(goalId, body);
    await refreshAll();
  }

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

  // COMPLETE GOAL
  async function completeGoal(goalId: number) {
    localStorage.setItem("userId", "1");
    const userId = localStorage.getItem("userId");
    if (!userId) {
      alert("User ID not found.");
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/users/${userId}/goals/${goalId}/complete`,
        { method: "POST" }
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText);
      }

      // Read plain text response, NOT json
      const message = await response.text();
      alert(message);

      await refreshAll();
    } catch (err: any) {
      console.error(err);
      alert(`Error completing goal: ${err.message}`);
    }
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-8 grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div className="rounded-2xl border border-slate-200 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xl font-semibold">Goals</h2>
        </div>
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
            <div key={g.goalId} className="flex items-center justify-between">
              <GoalCard
                key={g.goalId}
                goal={{
                  goalId: g.goalId,
                  title: g.title,
                  description: g.description ?? null,
                  priority: g.priority,
                }}
                onRequestDelete={() => requestDelete(g)}
                onEdit={() => setEditingGoal(g)}
              />
              <button
                onClick={() => completeGoal(g.goalId)}
                className="ml-4 px-2 py-1 bg-green-600 text-white rounded hover:bg-green-700"
                type="button"
              >
                Complete Goal
              </button>
            </div>
          ))}
        </div>
      </div>

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
            className={`w-full rounded-xl bg-black text-white py-2 font-medium hover:opacity-90 ${creating ? "opacity-70 cursor-not-allowed" : ""
              }`}
          >
            {creating ? "Saving…" : "Create"}
          </button>
        </form>
      </div>

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
              throw e;
            }
          }}
        />
      )}

      <ConfirmDialog
        open={!!toDelete}
        title="Delete goal"
        message={toDelete ? `Delete “${toDelete.title}”? This cannot be undone.` : ""}
        confirmText={deleting ? "Deleting…" : "Delete"}
        onConfirm={deleting ? () => { } : confirmDelete}
        onCancel={() => (deleting ? null : setToDelete(null))}
      />
    </div>
  );
}
