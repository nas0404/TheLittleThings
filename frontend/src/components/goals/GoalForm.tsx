import * as React from "react";
import { ApiError } from "../../api/http";
import type {
  Priority,
  CreateGoalRequest,
  UpdateGoalRequest,
} from "../../api/GoalApi";

// Type for category selection options
type CategoryOpt = { id: number; name: string };

// Base props shared between create and edit modes
type BaseProps = {
  initial?: Partial<CreateGoalRequest & UpdateGoalRequest>;
  categoryOptions: CategoryOpt[];
  lockCategoryId?: number;
};

// Props specific to create mode
type CreateProps = BaseProps & {
  mode: "create";
  submitText?: string;
  onSubmit: (values: Required<CreateGoalRequest>) => Promise<void> | void;
};

// Props specific to edit mode
type EditProps = BaseProps & {
  mode: "edit";
  submitText?: string;
  onSubmit: (values: UpdateGoalRequest) => Promise<void> | void;
};

// Union type for all possible props
type Props = CreateProps | EditProps;

// Available priority levels
const PRIOS: Priority[] = ["HIGH", "MEDIUM", "LOW"];



// Form component for creating or editing goals
export default function GoalForm(props: Props) {
  const { initial, categoryOptions, lockCategoryId } = props;

  // Form field states with initial values
  const [categoryId, setCategoryId] = React.useState<number | undefined>(
    lockCategoryId ?? initial?.categoryId
  );
  const [title, setTitle] = React.useState(initial?.title ?? "");
  const [description, setDescription] = React.useState<string>(
    (initial?.description ?? "") as string
  );
  const [priority, setPriority] = React.useState<Priority>(
    (initial?.priority as Priority) ?? "MEDIUM"
  );

  // Form error and submission states
  const [fieldErrs, setFieldErrs] = React.useState<Record<string, string>>({});
  const [formErr, setFormErr] = React.useState<string | null>(null);
  const [submitting, setSubmitting] = React.useState(false);

  // Map server-side validation errors to form fields
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

  // Handle form submission with validation
  const handleSubmit: React.FormEventHandler = async (e) => {
    e.preventDefault();
    // Reset error states
    setFormErr(null);
    setFieldErrs({});

    const t = title.trim();

    // Client-side validation
    if (props.mode === "create" && !categoryId) {
      return setFieldErrs({ categoryId: "category is required" });
    }
    if (!t) return setFieldErrs({ title: "title is required" });
    if (t.length > 255) return setFieldErrs({ title: "title must be ≤ 255 characters" });
    if (!PRIOS.includes(priority)) return setFieldErrs({ priority: "invalid priority" });
    if (description && description.length > 1000)
      return setFieldErrs({ description: "description must be ≤ 1000 characters" });

    setSubmitting(true);
    try {
      if (props.mode === "create") {
        // Prepare payload for creating new goal
        const payload: Required<CreateGoalRequest> = {
          categoryId: categoryId!, // guaranteed above by validation
          title: t,
          description: description || null,
          priority,
        };
        await props.onSubmit(payload);
      } else {
        // Prepare payload for updating existing goal
        const payload: UpdateGoalRequest = {
          categoryId,
          title: t,
          description: description || null,
          priority,
        };
        await props.onSubmit(payload);
      }
    } catch (err: any) {
      // Handle API errors
      if (err instanceof ApiError) {
        if (err.status === 409) {
          // Handle duplicate title error
          setFieldErrs({ title: err.details?.message || "title already exists" });
        } else {
          // Map other API validation errors to form fields
          const mapped = mapServerErrors(err.details);
          if (Object.keys(mapped).length) setFieldErrs(mapped);
          else setFormErr(err.details?.message || err.message || "Request failed");
        }
      } else {
        // Handle non-API errors
        setFormErr(err?.message || "Request failed");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    // Form container with vertical spacing
    <form onSubmit={handleSubmit} className="space-y-3">
      {props.mode === "create" && !lockCategoryId && (
        <div>
          <label className="block text-sm mb-1">Category</label>
          <select
            value={categoryId ?? ""}
            onChange={(e) => setCategoryId(Number(e.target.value) || undefined)}
            className={`w-full rounded-xl border px-3 py-2 text-sm ${
              fieldErrs.categoryId ? "border-red-400" : ""
            }`}
          >
            <option value="">Select…</option>
            {categoryOptions.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          {fieldErrs.categoryId && (
            <p className="text-xs text-red-600 mt-1">{fieldErrs.categoryId}</p>
          )}
        </div>
      )}

      <div>
        <label className="block text-sm mb-1">Title</label>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className={`w-full rounded-xl border px-3 py-2 text-sm ${
            fieldErrs.title ? "border-red-400" : ""
          }`}
          placeholder="e.g., Run 5km"
        />
        {fieldErrs.title && <p className="text-xs text-red-600 mt-1">{fieldErrs.title}</p>}
      </div>

      <div>
        <label className="block text-sm mb-1">Description</label>
        <textarea
          rows={3}
          value={description || ""}
          onChange={(e) => setDescription(e.target.value)}
          className={`w-full rounded-xl border px-3 py-2 text-sm ${
            fieldErrs.description ? "border-red-400" : ""
          }`}
          placeholder="Optional"
        />
        {fieldErrs.description && (
          <p className="text-xs text-red-600 mt-1">{fieldErrs.description}</p>
        )}
      </div>

      <div>
        <label className="block text-sm mb-1">Priority</label>
        <select
          value={priority}
          onChange={(e) => setPriority(e.target.value as Priority)}
          className={`w-full rounded-xl border px-3 py-2 text-sm ${
            fieldErrs.priority ? "border-red-400" : ""
          }`}
        >
          {PRIOS.map((p) => (
            <option key={p} value={p}>
              {p}
            </option>
          ))}
        </select>
        {fieldErrs.priority && (
          <p className="text-xs text-red-600 mt-1">{fieldErrs.priority}</p>
        )}
      </div>

      {formErr && <div className="text-sm text-red-600">{formErr}</div>}

      <button
        disabled={submitting}
        className={`w-full rounded-2xl px-4 py-2 text-sm font-medium shadow-sm transition bg-black text-white ${
          submitting ? "opacity-70" : ""
        }`}
      >
        {props.submitText ?? (props.mode === "create" ? "Create" : "Update")}
      </button>
    </form>
  );
}
