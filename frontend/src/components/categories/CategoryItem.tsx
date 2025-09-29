import type { Category } from  "../../api/CategoryApi";

function CategoryItem({
  category,
  onEdit,
  onDelete,
}: {
  category: Category;
  onEdit: (c: Category) => void;
  onDelete: (id: number) => void;
}) {
  return (
    <div className="flex items-center justify-between rounded-xl border p-3">
      <div>
        <div className="font-medium">{category.name}</div>
        {category.description && (
          <div className="text-sm text-gray-500">{category.description}</div>
        )}
      </div>
      <div className="flex items-center gap-2">
        <button
          className="rounded-2xl bg-gray-100 px-3 py-1 text-sm"
          onClick={() => onEdit(category)}
        >
          Edit
        </button>
        <button
          className="rounded-2xl bg-red-50 px-3 py-1 text-sm text-red-700"
          onClick={() => onDelete(category.id)}
        >
          Delete
        </button>
      </div>
    </div>
  );
}

export default CategoryItem;
