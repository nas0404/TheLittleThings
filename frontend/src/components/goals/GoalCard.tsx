// Type for goal priority levels
type Priority = "HIGH" | "MEDIUM" | "LOW";

// Lightweight goal type for card display
type GoalLite = {
  goalId: number;
  title: string;
  description: string | null;
  priority: Priority;
};

// Tailwind CSS classes for priority badges
const priClasses: Record<Priority, string> = {
  HIGH: "bg-red-100 text-red-700",
  MEDIUM: "bg-amber-100 text-amber-700",
  LOW: "bg-emerald-100 text-emerald-700",
};

// Props for the GoalCard component
type Props = {
  goal: GoalLite;
  onEdit: () => void;
  onRequestDelete: () => void;
  onComplete?: () => void; // FIX: support in-card complete button
};

// Component for displaying a goal in a card format
export default function GoalCard({ goal, onEdit, onRequestDelete, onComplete }: Props) {
  return (
    <div className="border rounded-xl p-4 bg-white">
      {/* Top: title, priority, id (wrap safely) */}
      <div className="min-w-0">
        <div className="flex items-center gap-2 flex-wrap">
          <div className="font-semibold break-words">{goal.title}</div>
          <span className={`text-2xs px-2 py-0.5 rounded ${priClasses[goal.priority]}`}>
            {goal.priority}
          </span>
          <span className="text-2xs text-slate-400">#{goal.goalId}</span>
        </div>

        {goal.description && (
          <div className="text-sm text-slate-600 mt-1 break-words">{goal.description}</div>
        )}
      </div>

      {/* Bottom: actions row (separate line, right-aligned) */}
      <div className="mt-3 flex flex-wrap items-center gap-2 justify-end">
        {onComplete && (
          <button
            className="px-3 py-1 rounded-lg bg-green-600 text-white hover:bg-green-700"
            type="button"
            onClick={onComplete}
          >
            Complete
          </button>
        )}
        <button
          className="px-3 py-1 rounded-lg border border-slate-300 hover:bg-slate-50"
          type="button"
          onClick={onEdit}
        >
          Edit
        </button>
        <button
          className="px-3 py-1 rounded-lg bg-rose-600 text-white hover:bg-rose-700"
          type="button"
          onClick={onRequestDelete}
        >
          Delete
        </button>
      </div>
    </div>
  );
}
