import * as React from "react";
import { ApiError } from "../../api/http";

type Values = { name: string; description?: string | null };
type Props = {
  initial?: Partial<Values>;
  submitText?: string;
  onSubmit: (values: Values) => Promise<void> | void;
  onCancel?: () => void;
};

export default function CategoryForm({
  initial,
  submitText = "Create",
  onSubmit,
  onCancel,
}: Props) {

  // Form state for fields, errors, and submission status
  const [name, setName] = React.useState(initial?.name ?? "");
  const [description, setDescription] = React.useState(initial?.description ?? "");
  const [fieldErrs, setFieldErrs] = React.useState<Record<string, string>>({});
  const [formErr, setFormErr] = React.useState<string | null>(null);
  const [submitting, setSubmitting] = React.useState(false);

  // When `initial` changes (e.g., switching from create → edit), reset form state
  React.useEffect(() => {
    setName(initial?.name ?? "");
    setDescription(initial?.description ?? "");
    setFieldErrs({});
    setFormErr(null);
  }, [initial?.name, initial?.description]);

  const canSubmit = name.trim().length > 0;

  // Map backend validation errors into { [field]: message } shape
  const mapServerErrors = (details?: any) => {
    const map: Record<string, string> = {};
    const arr = details?.errors;
    if (Array.isArray(arr)) {
      for (const e of arr) {
        const key = e.field || e.param || null;
        const msg = e.message || details?.message;
        if (key && msg && !map[key]) map[key] = msg;
      }
    }
    return map;
  };

  // Handle form submission
  const handleSubmit: React.FormEventHandler<HTMLFormElement> = async (e) => {
    e.preventDefault();
    setFormErr(null);
    setFieldErrs({});

    // Client-side validation
    const trimmed = name.trim();
    if (!trimmed) return setFieldErrs({ name: "name is required" });
    if (trimmed.length > 100) return setFieldErrs({ name: "name must be ≤ 100 characters" });
    if ((description || "").length > 100)
      return setFieldErrs({ description: "description must be ≤ 100 characters" });

    //Pass Payload to parent onSubmit handler
    setSubmitting(true);
    try {
      await onSubmit({ name: trimmed, description: description || null });
      if (!onCancel) {
        setName("");
        setDescription("");
      }
    }
    // Standardized error handling from our http() helper
    catch (err: any) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          setFieldErrs({ name: err.details?.message || "Category name already exists for this user" });
        } else {
          // Try to map field-level errors; fallback to a form-level error
          const mapped = mapServerErrors(err.details);
          if (Object.keys(mapped).length) setFieldErrs(mapped);
          else setFormErr(err.details?.message || err.message || "Request failed");
        }
      } else {
        setFormErr(err?.message || "Something went wrong");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <div>
        <label htmlFor="cat-name" className="block text-sm mb-1">Name</label>
        <input
          id="cat-name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="e.g., Health"
          maxLength={101}
          className={`w-full rounded-xl border px-3 py-2 text-sm ${fieldErrs.name ? "border-red-400 focus:border-red-500" : ""}`}
        />
        {fieldErrs.name && <div className="mt-1 text-xs text-red-600">{fieldErrs.name}</div>}
      </div>

      <div>
        <label htmlFor="cat-desc" className="block text-sm mb-1">Description - Optional</label>
        <textarea
          id="cat-desc"
          rows={4}
          value={description || ""}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Optional"
          maxLength={101}
          className={`w-full rounded-xl border px-3 py-2 text-sm ${fieldErrs.description ? "border-red-400 focus:border-red-500" : ""}`}
        />
        {fieldErrs.description && <div className="mt-1 text-xs text-red-600">{fieldErrs.description}</div>}
      </div>

      {formErr && <div className="text-sm text-red-600">{formErr}</div>}

      <div className="flex gap-2">
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="rounded-2xl bg-gray-100 px-4 py-2 text-sm"
            disabled={submitting}
          >
            Cancel
          </button>
        )}
        <button
          disabled={!canSubmit || submitting}
          className={`ml-auto w-full rounded-2xl px-4 py-2 text-sm font-medium shadow-sm transition ${canSubmit ? "bg-black text-white" : "bg-gray-200 text-gray-500"} ${submitting ? "opacity-70" : ""}`}
        >
          {submitText}
        </button>
      </div>
    </form>
  );
}
