// src/components/categories/EditCategoryModal.tsx
import React, { useState } from "react";
import type { Category, UpdateCategoryRequest } from "../../api/CategoryApi";
import { mapServerErrors } from "../../lib/mapServerErrors";

type Props = {
  category: Category;
  onClose: () => void;
  onSaved: (payload: UpdateCategoryRequest) => Promise<void>;
};

export default function EditCategoryModal({ category, onClose, onSaved }: Props) {
  const [name, setName] = useState(category.name);
  const [description, setDescription] = useState(category.description ?? "");
  const [saving, setSaving] = useState(false);
  const [errs, setErrs] = useState<Record<string, string>>({});

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setErrs({});
    try {
      // both fields are fine; backend can treat them as full or partial update
      await onSaved({ name, description: description ? description : null });
    } catch (e: any) {
      const mapped = mapServerErrors(e?.details);
      setErrs(Object.keys(mapped).length ? mapped : { form: e?.message || "Update failed" });
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 bg-black/30 z-50 flex items-center justify-center p-4">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-lg p-5">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-lg font-semibold">Edit Category</h3>
          <button className="text-slate-500" onClick={onClose} type="button">✕</button>
        </div>

        {errs.form && <div className="mb-3 text-sm text-red-600">{errs.form}</div>}

        <form onSubmit={submit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Name</label>
            <input
              className={`w-full rounded-lg border px-3 py-2 ${errs.name ? "border-red-400" : ""}`}
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            {errs.name && <p className="text-xs text-red-600 mt-1">{errs.name}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Description</label>
            <textarea
              className="w-full rounded-lg border px-3 py-2"
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div className="flex gap-2 justify-end pt-2">
            <button type="button" className="px-4 py-2 rounded-lg border" onClick={onClose} disabled={saving}>
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className={`px-4 py-2 rounded-lg bg-black text-white ${saving ? "opacity-70 cursor-not-allowed" : ""}`}
            >
              {saving ? "Saving…" : "Save changes"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
