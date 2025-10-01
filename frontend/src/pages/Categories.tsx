import { useEffect, useState } from "react";
import { CategoriesAPI } from "../api/CategoryApi";
import type {
  Category,
  CreateCategoryRequest,
  UpdateCategoryRequest,
} from "../api/CategoryApi";

import CategoryCard from "../components/categories/CategoryCard";
import EditCategoryModal from "../components/categories/EditCategoryModal";
import ConfirmDialog from "../components/ui/ConfirmDialog";
import CategoryNeglected from "../components/categories/CategoryNeglected";
import CategoryForm from "../components/categories/CategoryForm";

export default function CategoriesPage() {

  // State for categories, loading, errors, editing, deleting, and creating
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [pageErr, setPageErr] = useState<string | null>(null);

  const [editing, setEditing] = useState<Category | null>(null);

  const [toDelete, setToDelete] = useState<Category | null>(null);
  const [deleting, setDeleting] = useState(false);

  const [creating, setCreating] = useState(false);

  // Fetch categories from the API
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

  // Handle creation of a new category
  async function handleCreate(values: { name: string; description?: string | null }) {
    setCreating(true);
    const payload: CreateCategoryRequest = {
      name: values.name,
      description: values.description ?? null,
    };
    try {
      await CategoriesAPI.create(payload);
      await refresh();
    }
    finally {
      setCreating(false);
    }
  }

  // Request deletion of a category
  function requestDelete(category: Category) {
    setToDelete(category);
  }

  // Confirm deletion of the selected category
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
      <div className="rounded-2xl border border-slate-200 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xl font-semibold">Categories</h2>
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

      <div className="rounded-2xl border border-slate-200 p-5">
        <h2 className="text-xl font-semibold mb-3">New Category</h2>


        <CategoryForm
          submitText={creating ? "Saving…" : "Create"}
          onSubmit={handleCreate}
        />
      </div>

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
              throw e;
            }
          }}
        />
      )}

      <ConfirmDialog
        open={!!toDelete}
        title="Delete category"
        message={toDelete ? `Delete “${toDelete.name}”? This cannot be undone.` : ""}
        confirmText={deleting ? "Deleting…" : "Delete"}
        onConfirm={deleting ? () => { } : confirmDelete}
        onCancel={() => (deleting ? null : setToDelete(null))}
      />
    </div>
  );
}
