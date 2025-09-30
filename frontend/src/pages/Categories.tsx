// src/pages/Categories.tsx
import React, { useEffect, useState } from "react";
import { CategoriesAPI } from "../api/CategoryApi";
import type {
  Category,
  CreateCategoryRequest,
  UpdateCategoryRequest,
} from "../api/CategoryApi";

import CategoryCard from "../components/categories/CategoryCard";
import EditCategoryModal from "../components/categories/EditCategoryModal";
import ConfirmDialog from "../components/ui/ConfirmDialog";
import { mapServerErrors } from "../lib/mapServerErrors";
import CategoryNeglected from "../components/categories/CategoryNeglected"; // ← add this

export default function CategoriesPage() {
  const devUserId = localStorage.getItem("devUserId") ?? "—";

  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [pageErr, setPageErr] = useState<string | null>(null);

  // create form
  // Create initial form state (right column)
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [creating, setCreating] = useState(false);
  const [createErrs, setCreateErrs] = useState<Record<string, string>>({});

  // edit modal
  const [editing, setEditing] = useState<Category | null>(null);

  // in-app delete confirm
  const [toDelete, setToDelete] = useState<Category | null>(null);
  const [deleting, setDeleting] = useState(false);

  // Load categories from API
  async function refresh() {
    setLoading(true);
    setPageErr(null);
    try {
      const data = await CategoriesAPI.list();
      setCategories(data);
    } catch (e: any) {
      setPageErr(e?.message ?? "Failed to load categories");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
  }, []);


  /*All the functions below are calling an API
   and handling the results/errors appropriately.
  */

  // Handle create form submission
  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setCreating(true);
    setCreateErrs({});
    const payload: CreateCategoryRequest = {
      name,
      description: description ? description : null,
    };
    try {
      await CategoriesAPI.create(payload);
      setName("");
      setDescription("");
      await refresh();
    } catch (e: any) {
      const mapped = mapServerErrors(e?.details);
      setCreateErrs(Object.keys(mapped).length ? mapped : { form: e?.message || "Create failed" });
    } finally {
      setCreating(false);
    }
  }

  // request delete from card
  function requestDelete(category: Category) {
    setToDelete(category);
  }
  // confirm delete from ConfirmDialog
  async function confirmDelete() {
    if (!toDelete) return;
    setDeleting(true);
    try {
      await CategoriesAPI.remove(toDelete.id);
      setToDelete(null);
      await refresh();
    } catch (e: any) {
      alert(e?.message ?? "Delete failed");
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="max-w-5xl mx-auto px-6 py-8 grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Left: list */}
      <div className="rounded-2xl border border-slate-200 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xl font-semibold">Categories</h2>
          <span className="text-xs text-slate-500">dev user: {devUserId}</span>
        </div>

        {loading && <div className="text-sm text-slate-500">Loading…</div>}
        {pageErr && <div className="text-sm text-red-600">{pageErr}</div>}

        {!loading && !pageErr && categories.length === 0 && (
          <div className="text-sm text-slate-500">No categories yet. Add one on the right.</div>
        )}

        <div className="space-y-3">
          {categories.map((c) => (
            <CategoryCard
              key={c.id}
              category={c}
              onEdit={() => setEditing(c)}
              onRequestDelete={() => requestDelete(c)}
            />
          ))}
        </div>
      </div>

      {/* Right: create */}
      <div className="rounded-2xl border border-slate-200 p-5">
        <h2 className="text-xl font-semibold mb-3">New Category</h2>

        {createErrs.form && <div className="mb-3 text-sm text-red-600">{createErrs.form}</div>}

        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Name</label>
            <input
              className={`w-full rounded-lg border px-3 py-2 ${createErrs.name ? "border-red-400" : ""}`}
              placeholder="e.g., Health"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            {createErrs.name && <p className="text-xs text-red-600 mt-1">{createErrs.name}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Description</label>
            <textarea
              className="w-full rounded-lg border px-3 py-2"
              placeholder="Optional"
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
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

      {/* Neglected categories (third card in grid) */}
      <CategoryNeglected />

      {editing && (
        <EditCategoryModal
          category={editing}
          onClose={() => setEditing(null)}
          onSaved={async (payload: UpdateCategoryRequest) => {
            try {
              await CategoriesAPI.update(editing.id, payload);
              setEditing(null);
              await refresh();
            } catch (e) {
              throw e; // handled in the modal
            }
          }}
        />
      )}

      {/* in-app confirm dialog for delete */}
      <ConfirmDialog
        open={!!toDelete}
        title="Delete category"
        message={toDelete ? `Delete “${toDelete.name}”? This cannot be undone.` : ""}
        confirmText={deleting ? "Deleting…" : "Delete"}
        onConfirm={deleting ? () => {} : confirmDelete}
        onCancel={() => (deleting ? null : setToDelete(null))}
      />
    </div>
  );
}
