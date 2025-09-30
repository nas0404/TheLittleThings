import React, { useState } from "react";
import { mapServerErrors } from  "../../lib/mapServerErrors";

type Priority = "HIGH" | "MEDIUM" | "LOW";

type CategoryLite = {
  categoryId: number;
  name: string;
};

type GoalForEdit = {
  goalId: number;
  title: string;
  description: string | null;
  priority: Priority;
  categoryId: number;
};

export type UpdateGoalRequest = Partial<{
  title: string;
  description: string | null;
  priority: Priority;
  categoryId: number;
}>;

type Props = {
  goal: GoalForEdit;
  categories: CategoryLite[];
  onClose: () => void;
  onSave: (payload: UpdateGoalRequest) => Promise<void>;
};

export default function EditGoalModal({
  goal,
  categories,
  onClose,
  onSave,
}: Props) {
  const [title, setTitle] = useState<string>(goal.title);
  const [description, setDescription] = useState<string>(goal.description ?? "");
  const [priority, setPriority] = useState<Priority>(goal.priority);
  const [categoryIdStr, setCategoryIdStr] = useState<string>(String(goal.categoryId));
  const [saving, setSaving] = useState(false);
  const [errs, setErrs] = useState<Record<string, string>>({});

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setErrs({});

    const payload: UpdateGoalRequest = {
      title,
      description: description ? description : null,
      priority,
      categoryId: categoryIdStr ? Number(categoryIdStr) : undefined,
    };

    try {
      await onSave(payload);
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
          <h3 className="text-lg font-semibold">Edit Goal</h3>
          <button className="text-slate-500" onClick={onClose} type="button">✕</button>
        </div>

        {errs.form && <div className="mb-3 text-sm text-red-600">{errs.form}</div>}

        <form onSubmit={submit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Category</label>
            <select
              className={`w-full rounded-lg border px-3 py-2 ${errs.categoryId ? "border-red-400" : ""}`}
              value={categoryIdStr}
              onChange={(e) => setCategoryIdStr(e.target.value)}
            >
              {categories.map((c) => (
                <option key={c.categoryId} value={String(c.categoryId)}>
                  {c.name}
                </option>
              ))}
            </select>
            {errs.categoryId && <p className="text-xs text-red-600 mt-1">{errs.categoryId}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Title</label>
            <input
              className={`w-full rounded-lg border px-3 py-2 ${errs.title ? "border-red-400" : ""}`}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
            {errs.title && <p className="text-xs text-red-600 mt-1">{errs.title}</p>}
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

          <div>
            <label className="block text-sm font-medium mb-1">Priority</label>
            <select
              className="w-full rounded-lg border px-3 py-2"
              value={priority}
              onChange={(e) => setPriority(e.target.value as Priority)}
            >
              <option value="HIGH">HIGH</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="LOW">LOW</option>
            </select>
          </div>

          <div className="flex gap-2 justify-end pt-2">
            <button type="button" className="px-4 py-2 rounded-lg border" onClick={onClose} disabled={saving}>
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className={`px-4 py-2 rounded-lg bg-black text-white ${
                saving ? "opacity-70 cursor-not-allowed" : ""
              }`}
            >
              {saving ? "Saving…" : "Save changes"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
