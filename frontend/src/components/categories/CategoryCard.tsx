import type { Category } from "../../api/CategoryApi";

// Props expected by the CategoryCard
type Props = {
  category: Category;
  onEdit: () => void;
  onRequestDelete: () => void;
};

// Component to render a single category card
export default function CategoryCard({ category, onEdit, onRequestDelete }: Props) {
  return (
    <div className="border rounded-xl p-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <div className="font-semibold">{category.name}</div>
            <span className="text-2xs text-slate-400">#{category.id}</span>
          </div>
          {category.description && (
            <div className="text-sm text-slate-600 mt-1">{category.description}</div>
          )}
        </div>
        <div className="flex items-center gap-2">
          <button
            className="px-3 py-1 rounded-lg border border-slate-300"
            type="button"
            onClick={onEdit}
          >
            Edit
          </button>
          <button
            className="px-3 py-1 rounded-lg bg-rose-600 text-white"
            type="button"
            onClick={onRequestDelete}
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}
