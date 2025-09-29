import React from "react";
import type { Category } from  "../../api/CategoryApi";

function CategoryForm({
  initial,
  submitText = "Create",
  onSubmit,
}: {
  initial?: Partial<Pick<Category, "name" | "description">>;
  submitText?: string;
  onSubmit: (values: { name: string; description?: string | null }) => Promise<void> | void;
}) {
  const [name, setName] = React.useState(initial?.name ?? "");
  const [description, setDescription] = React.useState(initial?.description ?? "");
  const canSubmit = name.trim().length > 0;

  return (
    <form
      onSubmit={async (e) => {
        e.preventDefault();
        if (!canSubmit) return;
        await onSubmit({ name: name.trim(), description: description || null });
      }}
      className="space-y-3"
    >
      <div>
        <label className="block text-sm mb-1">Name</label>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="e.g., Health"
          className="w-full rounded-xl border px-3 py-2 text-sm"
        />
      </div>
      <div>
        <label className="block text-sm mb-1">Description</label>
        <textarea
          rows={4}
          value={description || ""}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Optional"
          className="w-full rounded-xl border px-3 py-2 text-sm"
        />
      </div>
      <button
        disabled={!canSubmit}
        className={`w-full rounded-2xl px-4 py-2 text-sm font-medium shadow-sm transition ${
          canSubmit ? "bg-black text-white" : "bg-gray-200 text-gray-500"
        }`}
      >
        {submitText}
      </button>
    </form>
  );
}

export default CategoryForm;