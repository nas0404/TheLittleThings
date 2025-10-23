import React, { useEffect, useMemo, useState, useCallback } from "react";

// Goals side
import GoalCard from "../components/goals/GoalCard";
import EditGoalModal, { type UpdateGoalRequest } from "../components/goals/EditGoalModal";
import { GoalsAPI, type GoalResponse, type Priority, type CreateGoalRequest } from "../api/GoalApi";
import { mapServerErrors } from "../lib/mapServerErrors";

// Categories side
import {
  CategoriesAPI,
  type Category,
  type CreateCategoryRequest,
  type UpdateCategoryRequest,
} from "../api/CategoryApi";
import CategoryCard from "../components/categories/CategoryCard";
import EditCategoryModal from "../components/categories/EditCategoryModal";
import CategoryNeglected from "../components/categories/CategoryNeglected";
import CategoryForm from "../components/categories/CategoryForm";

// Shared
import ConfirmDialog from "../components/ui/ConfirmDialog";

// Sorting options for goals
type SortKey = "createdAt_desc" | "priority_desc";

export default function CreateGoalsPage() {
  // Categories state
  const [categories, setCategories] = useState<Category[]>([]);
  const [catsLoading, setCatsLoading] = useState(true);
  const [catsErr, setCatsErr] = useState<string | null>(null);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [toDeleteCategory, setToDeleteCategory] = useState<Category | null>(null);
  const [deletingCategory, setDeletingCategory] = useState(false);
  const [creatingCategory, setCreatingCategory] = useState(false);

  // Goals state
  const [goals, setGoals] = useState<GoalResponse[]>([]);
  const [goalsLoading, setGoalsLoading] = useState(true);
  const [goalsErr, setGoalsErr] = useState<string | null>(null);
  const [editingGoal, setEditingGoal] = useState<GoalResponse | null>(null);
  const [toDeleteGoal, setToDeleteGoal] = useState<GoalResponse | null>(null);
  const [deletingGoal, setDeletingGoal] = useState(false);

  // Filters/sorting for goals
  const [filterCategoryIdStr, setFilterCategoryIdStr] = useState<string>("");
  const selectedFilterCategoryId = useMemo(
    () => (filterCategoryIdStr === "" ? undefined : Number(filterCategoryIdStr)),
    [filterCategoryIdStr]
  );
  const [sortBy, setSortBy] = useState<SortKey>("createdAt_desc");

  // New Goal form
  const [newCategoryIdStr, setNewCategoryIdStr] = useState<string>("");
  const [newTitle, setNewTitle] = useState("");
  const [newDescription, setNewDescription] = useState("");
  const [newPriority, setNewPriority] = useState<Priority>("MEDIUM");
  const [createErrors, setCreateErrors] = useState<Record<string, string>>({});
  const [creatingGoal, setCreatingGoal] = useState(false);

  // === Fetchers ===
  const refreshCategories = useCallback(async () => {
    setCatsLoading(true);
    setCatsErr(null);
    try {
      const data = await CategoriesAPI.list();
      setCategories(data);
      // If user has no category selected for the New Goal form, pick the first available
      if (!newCategoryIdStr && data.length > 0) {
        setNewCategoryIdStr(String(data[0].id));
      }
    } catch (e: any) {
      setCatsErr(e?.message ?? "Failed to load categories");
    } finally {
      setCatsLoading(false);
    }
  }, [newCategoryIdStr]);

  const refreshGoals = useCallback(async () => {
    setGoalsLoading(true);
    setGoalsErr(null);
    try {
      const data = await GoalsAPI.list({ categoryId: selectedFilterCategoryId });
      setGoals(data);
    } catch (e: any) {
      setGoalsErr(e?.message ?? "Failed to load goals");
    } finally {
      setGoalsLoading(false);
    }
  }, [selectedFilterCategoryId]);

  const refreshAll = useCallback(async () => {
    await Promise.all([refreshCategories(), refreshGoals()]);
  }, [refreshCategories, refreshGoals]);

  useEffect(() => {
    refreshAll();
  }, [refreshAll]);

  // Re-fetch goals when filter changes
  useEffect(() => {
    refreshGoals();
  }, [refreshGoals]);

  // === Derived/sorted goals ===
  const sortedGoals = useMemo(() => {
    const byNewest = (a: GoalResponse, b: GoalResponse) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();

    if (sortBy === "createdAt_desc") return [...goals].sort(byNewest);

    const weight: Record<Priority, number> = { HIGH: 0, MEDIUM: 1, LOW: 2 };
    return [...goals].sort((a, b) => {
      const wa = weight[a.priority] ?? 3;
      const wb = weight[b.priority] ?? 3;
      if (wa !== wb) return wa - wb;
      return byNewest(a, b);
    });
  }, [goals, sortBy]);

  // === Category handlers ===
  async function handleCreateCategory(values: { name: string; description?: string | null }) {
    setCreatingCategory(true);
    const payload: CreateCategoryRequest = {
      name: values.name,
      description: values.description ?? null,
    };
    try {
      const wasEmpty = categories.length === 0;
      await CategoriesAPI.create(payload);
      await refreshCategories();
      if (wasEmpty) {
        // If this was the very first category, default-select it for new goal creation
        const updated = await CategoriesAPI.list();
        setCategories(updated);
        if (updated.length > 0) setNewCategoryIdStr(String(updated[0].id));
      }
    } finally {
      setCreatingCategory(false);
    }
  }

  function requestDeleteCategory(category: Category) {
    setToDeleteCategory(category);
  }

  async function confirmDeleteCategory() {
    if (!toDeleteCategory) return;
    setDeletingCategory(true);
    try {
      await CategoriesAPI.remove(toDeleteCategory.id);
      setToDeleteCategory(null);
      await refreshCategories();
      // If we deleted the currently selected category for new-goal, reset selection
      if (newCategoryIdStr === String(toDeleteCategory.id)) {
        const updated = await CategoriesAPI.list();
        setCategories(updated);
        setNewCategoryIdStr(updated[0] ? String(updated[0].id) : "");
      }
    } catch (e: any) {
      alert(e?.message ?? "Delete failed");
    } finally {
      setDeletingCategory(false);
    }
  }

  // === Goal handlers ===
  async function handleCreateGoal(e: React.FormEvent) {
    e.preventDefault();
    setCreatingGoal(true);
    setCreateErrors({});

    const payload: Required<CreateGoalRequest> = {
      categoryId: Number(newCategoryIdStr),
      title: newTitle.trim(),
      description: newDescription ? newDescription : null,
      priority: newPriority,
    };

    try {
      await GoalsAPI.create(payload);
      setNewTitle("");
      setNewDescription("");
      setNewPriority("MEDIUM");
      await refreshGoals();
    } catch (e: any) {
      const mapped = mapServerErrors(e?.details);
      setCreateErrors(Object.keys(mapped).length ? mapped : { form: e?.message || "Create failed" });
    } finally {
      setCreatingGoal(false);
    }
  }

  async function handleUpdateGoal(goalId: number, body: UpdateGoalRequest) {
    await GoalsAPI.update(goalId, body);
    await refreshGoals();
  }

  function requestDeleteGoal(goal: GoalResponse) {
    setToDeleteGoal(goal);
  }

  async function confirmDeleteGoal() {
    if (!toDeleteGoal) return;
    setDeletingGoal(true);
    try {
      await GoalsAPI.remove(toDeleteGoal.goalId);
      setToDeleteGoal(null);
      await refreshGoals();
    } catch (e: any) {
      alert(e?.message ?? "Delete failed");
    } finally {
      setDeletingGoal(false);
    }
  }

  async function completeGoal(goalId: number) {
    const userId = localStorage.getItem("userId");
    const token = localStorage.getItem("token");

    if (!userId || !token) {
      alert("User ID or token not found.");
      return;
    }

    try {
      const response = await fetch(`/api/goals/${goalId}/complete`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText);
      }

      const message = await response.text();
      alert(message);
      await refreshGoals();
    } catch (err: any) {
      console.error(err);
      alert(`Error completing goal: ${err.message}`);
    }
  }

  return (
    <div className="max-w-7xl mx-auto px-6 py-8 grid grid-cols-1 lg:grid-cols-3 gap-6">
      {/* Column 1: Categories list */}
      <div className="rounded-2xl border border-slate-200 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xl font-semibold">Categories</h2>
        </div>

        {catsLoading && <div className="text-sm text-slate-500">Loading…</div>}
        {catsErr && <div className="text-sm text-red-600">{catsErr}</div>}

        {!catsLoading && !catsErr && categories.length === 0 && (
          <div className="text-sm text-slate-500">No categories yet. Create one on the right.</div>
        )}

        <div className="space-y-3">
          {categories.map((c) => (
            <CategoryCard
              key={c.id}
              category={c}
              onEdit={() => setEditingCategory(c)}
              onRequestDelete={() => requestDeleteCategory(c)}
            />
          ))}
        </div>

        <div className="mt-6">
          <CategoryNeglected />
        </div>
      </div>

      {/* Column 2: Goals list with filter/sort + complete */}
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

        {goalsLoading && <div className="text-sm text-slate-500">Loading…</div>}
        {goalsErr && <div className="text-sm text-red-600">{goalsErr}</div>}

        {!goalsLoading && !goalsErr && sortedGoals.length === 0 && (
          <div className="text-sm text-slate-500">No goals yet. Create one on the right.</div>
        )}

        <div className="space-y-3">
            {sortedGoals.map((g) => (
                <div key={g.goalId} className="flex items-center gap-3">
                    <div className="flex-1 min-w-0">
                        <GoalCard
                        goal={{
                            goalId: g.goalId,
                            title: g.title,
                            description: g.description ?? null,
                            priority: g.priority,
                        }}
                        onRequestDelete={() => requestDeleteGoal(g)}
                        onEdit={() => setEditingGoal(g)}
                        />
                    </div>

                    <button
                        onClick={() => completeGoal(g.goalId)}
                        className="shrink-0 self-center h-9 px-3 rounded bg-green-600 text-white hover:bg-green-700"
                        type="button"
                    >
                        Complete Goal
                    </button>
                </div>
            ))}
        </div>
    </div>

      {/* Column 3: New Category + New Goal forms */}
      <div className="space-y-6">
        <div className="rounded-2xl border border-slate-200 p-5">
          <h2 className="text-xl font-semibold mb-3">New Category</h2>
          <CategoryForm
            submitText={creatingCategory ? "Saving…" : "Create"}
            onSubmit={handleCreateCategory}
          />
        </div>

        <div className="rounded-2xl border border-slate-200 p-5">
          <h2 className="text-xl font-semibold mb-3">New Goal</h2>

          {createErrors.form && (
            <div className="mb-3 text-sm text-red-600">{createErrors.form}</div>
          )}

          <form onSubmit={handleCreateGoal} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">Category</label>
              <select
                className={`w-full rounded-lg border px-3 py-2 ${
                  createErrors.categoryId ? "border-red-400" : ""
                }`}
                value={newCategoryIdStr}
                onChange={(e) => setNewCategoryIdStr(e.target.value)}
                disabled={categories.length === 0}
              >
                {categories.length === 0 ? (
                  <option value="">Create a category first…</option>
                ) : (
                  <>
                    {categories.map((c) => (
                      <option key={c.id} value={String(c.id)}>
                        {c.name}
                      </option>
                    ))}
                  </>
                )}
              </select>
              {createErrors.categoryId && (
                <p className="text-xs text-red-600 mt-1">{createErrors.categoryId}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Title</label>
              <input
                className={`w-full rounded-lg border px-3 py-2 ${
                  createErrors.title ? "border-red-400" : ""
                }`}
                placeholder="e.g., Run 5km"
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                disabled={categories.length === 0}
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
                disabled={categories.length === 0}
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Priority</label>
              <select
                className="w-full rounded-lg border px-3 py-2"
                value={newPriority}
                onChange={(e) => setNewPriority(e.target.value as Priority)}
                disabled={categories.length === 0}
              >
                <option value="HIGH">HIGH</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="LOW">LOW</option>
              </select>
            </div>

            <button
              type="submit"
              disabled={creatingGoal || categories.length === 0}
              className={`w-full rounded-xl bg-black text-white py-2 font-medium hover:opacity-90 ${
                creatingGoal || categories.length === 0 ? "opacity-70 cursor-not-allowed" : ""
              }`}
            >
              {creatingGoal ? "Saving…" : "Create"}
            </button>
          </form>
        </div>
      </div>

      {/* Modals & confirms */}
      {editingCategory && (
        <EditCategoryModal
          category={editingCategory}
          onClose={() => setEditingCategory(null)}
          onSaved={async (payload: UpdateCategoryRequest) => {
            try {
              await CategoriesAPI.update(editingCategory.id, payload);
              setEditingCategory(null);
              await refreshCategories();
            } catch (e) {
              throw e;
            }
          }}
        />
      )}

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
        open={!!toDeleteCategory}
        title="Delete category"
        message={
          toDeleteCategory ? `Delete “${toDeleteCategory.name}”? This cannot be undone.` : ""
        }
        confirmText={deletingCategory ? "Deleting…" : "Delete"}
        onConfirm={deletingCategory ? () => {} : confirmDeleteCategory}
        onCancel={() => (deletingCategory ? null : setToDeleteCategory(null))}
      />

      <ConfirmDialog
        open={!!toDeleteGoal}
        title="Delete goal"
        message={toDeleteGoal ? `Delete “${toDeleteGoal.title}”? This cannot be undone.` : ""}
        confirmText={deletingGoal ? "Deleting…" : "Delete"}
        onConfirm={deletingGoal ? () => {} : confirmDeleteGoal}
        onCancel={() => (deletingGoal ? null : setToDeleteGoal(null))}
      />
    </div>
  );
}
