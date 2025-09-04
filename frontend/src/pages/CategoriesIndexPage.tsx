// src/pages/CategoriesIndexPage.tsx
import { useEffect, useState, type JSX } from "react";
import { useNavigate } from "react-router-dom";

/** Category type (matches your backend shape) */
type Category = {
  categoryId: number;
  userId: number;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
};

/** mock data */
const INITIAL_CATEGORIES: Category[] = [
  { categoryId: 1, userId: 100, name: "Fitness", description: "Health & training" },
  { categoryId: 2, userId: 100, name: "Career", description: "Work & growth" },
  { categoryId: 3, userId: 101, name: "Learning", description: "Study & skills" },
  { categoryId: 4, userId: 102, name: "Finance", description: "Budgeting & savings" },
  { categoryId: 5, userId: 103, name: "Mindfulness", description: "Meditation & journaling" },
  { categoryId: 6, userId: 104, name: "Nutrition", description: "Healthy eating habits" },
  { categoryId: 7, userId: 100, name: "Family", description: "Time with loved ones" },
  { categoryId: 8, userId: 105, name: "Travel", description: "Exploring new places" },
];

/** Minimal modal (inline) */
function Modal({
  open,
  onClose,
  children,
}: { open: boolean; onClose: () => void; children: React.ReactNode }) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50">
      <div className="absolute inset-0 bg-black/30" onClick={onClose} />
      <div
        className="absolute left-1/2 top-1/2 w-[min(90vw,520px)] -translate-x-1/2 -translate-y-1/2 rounded-xl border bg-white p-4 shadow-lg"
        role="dialog"
        aria-modal="true"
      >
        {children}
      </div>
    </div>
  );
}

export default function CategoriesIndexPage(): JSX.Element {
  const navigate = useNavigate();

  // list state
  const [categories, setCategories] = useState<Category[]>(INITIAL_CATEGORIES);

  // edit modal state
  const [editing, setEditing] = useState<Category | null>(null);
  const [form, setForm] = useState<{ name: string; description: string }>({
    name: "",
    description: "",
  });

  // prime form when opening modal
  useEffect(() => {
    if (editing) {
      setForm({
        name: editing.name,
        description: editing.description ?? "",
      });
    }
  }, [editing]);

  // handlers
  const onRemove = (categoryId: number) => {
    if (!confirm("Remove this category? (placeholder only)")) return;
    setCategories(prev => prev.filter(c => c.categoryId !== categoryId));
  };

  const onUpdateClick = (cat: Category) => setEditing(cat);

  const onSubmitEdit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!editing) return;
    const name = form.name.trim();
    const description = form.description.trim();
    if (!name) return;

    setCategories(prev =>
      prev.map(c =>
        c.categoryId === editing.categoryId
          ? {
            ...c,
            name,
            description: description || undefined,
            updatedAt: new Date().toISOString(),
          }
          : c
      )
    );
    setEditing(null);
  };

  return (
    <div className="p-6">
      <div className="mb-5 flex items-center justify-between">
        <h1 className="text-xl font-semibold">Categories</h1>
        <button
          className="h-9 rounded-md bg-gray-900 px-3 text-white"
          onClick={() => alert("TODO: open New Category modal")}
        >
          + New Category
        </button>
      </div>

      {categories.length === 0 ? (
        <div className="rounded-xl border bg-white p-10 text-center text-gray-600">
          No categories yet. Create your first one.
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map(c => (
            <button
              key={c.categoryId}
              onClick={() => navigate(`/categories/${c.categoryId}`)}
              className="rounded-xl border bg-white p-4 text-left shadow-sm transition hover:shadow"
            >
              <div className="mb-1 flex items-center justify-between">
                <div className="font-medium">{c.name}</div>
                <span className="text-xs text-gray-400">User {c.userId}</span>
              </div>

              <div className="text-sm text-gray-600">
                {c.description ?? "No description"}
              </div>

              <div className="mt-3 flex gap-2">
                <button
                  type="button"
                  onClick={e => {
                    e.stopPropagation();
                    onUpdateClick(c);
                  }}
                  className="rounded-md border px-2 py-1 text-sm hover:bg-gray-50"
                >
                  Update details
                </button>
                <button
                  type="button"
                  onClick={e => {
                    e.stopPropagation();
                    onRemove(c.categoryId);
                  }}
                  className="rounded-md border px-2 py-1 text-sm text-red-600 hover:bg-red-50"
                >
                  Remove
                </button>
              </div>
            </button>
          ))}
        </div>
      )}

      {/* Edit Modal */}
      <Modal open={!!editing} onClose={() => setEditing(null)}>
        <h2 className="mb-3 text-lg font-semibold">Update category</h2>
        <form onSubmit={onSubmitEdit} className="space-y-3">
          <div>
            <label className="mb-1 block text-sm font-medium">Name</label>
            <input
              value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
              required
              className="w-full rounded-md border px-3 py-2"
              placeholder="Fitness"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Description</label>
            <textarea
              value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
              className="w-full rounded-md border px-3 py-2"
              rows={3}
              placeholder="What is this category for?"
            />
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setEditing(null)}
              className="rounded-md border px-3 py-2"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="rounded-md bg-gray-900 px-3 py-2 text-white"
            >
              Save
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
