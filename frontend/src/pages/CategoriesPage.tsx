
import * as React from "react";
import Card from "../components/ui/Card";
import { CategoriesAPI, type Category } from "../api/CategoryApi";
import CategoryItem from "../components/categories/CategoryItem";
import CategoryForm from "../components/categories/CategoryForm";
import CategoryNeglected from "../components/categories/CategoryNeglected";

// Single row renderer for a category (kept simple & reusable)

// Optional sidebar widget that calls /neglected

export default function CategoriesPage() {
  const [loading, setLoading] = React.useState(true);
  const [categories, setCategories] = React.useState<Category[]>([]);
  const [editing, setEditing] = React.useState<Category | null>(null);
  const [err, setErr] = React.useState<string | null>(null);

  const load = React.useCallback(async () => {
    try {
      setLoading(true);
      setErr(null);
      const data = await CategoriesAPI.list();
      setCategories(data);
    } catch (e: any) {
      setErr(e?.message || "Failed to load categories");
    } finally {
      setLoading(false);
    }
  }, []);

  React.useEffect(() => {
    load();
  }, [load]);

  const onCreate = async (values: { name: string; description?: string | null }) => {
    setErr(null);
    await CategoriesAPI.create(values);
    await load();
  };

  const onUpdate = async (values: { name: string; description?: string | null }) => {
    if (!editing) return;
    setErr(null);
    await CategoriesAPI.update(editing.id, values);
    setEditing(null);
    await load();
  };

  const onDelete = async (id: number) => {
    setErr(null);
    await CategoriesAPI.remove(id);
    await load();
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div className="lg:col-span-2 space-y-3">
        <Card title="Categories" description="Browse, edit, or remove your categories.">
          {loading && <div className="text-sm text-gray-500">Loading…</div>}
          {err && <div className="text-sm text-red-600">{err}</div>}
          {!loading && categories.length === 0 && (
            <div className="text-sm text-gray-500">No categories yet.</div>
          )}

          <div className="space-y-2 mt-2">
            {categories.map((c) => (
              <CategoryItem
                key={c.id}
                category={c}
                onEdit={setEditing}
                onDelete={onDelete}
              />
            ))}
          </div>
        </Card>
      </div>

      <div className="space-y-6">
        <Card
          title={editing ? "Edit Category" : "New Category"}
          description={editing ? "Update the selected category." : "Create a new category."}
        >
          <CategoryForm
            initial={editing ?? undefined}
            submitText={editing ? "Update" : "Create"}
            onSubmit={editing ? onUpdate : onCreate}
          />
        </Card>
        <CategoryNeglected/>
      </div>
    </div>
  );
}
