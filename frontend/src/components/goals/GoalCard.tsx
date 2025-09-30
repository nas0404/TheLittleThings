import React from "react";

type Priority = "HIGH" | "MEDIUM" | "LOW";

type GoalLite = {
  goalId: number;
  title: string;
  description: string | null;
  priority: Priority;
};

const priClasses: Record<Priority, string> = {
  HIGH: "bg-red-100 text-red-700",
  MEDIUM: "bg-amber-100 text-amber-700",
  LOW: "bg-emerald-100 text-emerald-700",
};

type Props = {
  goal: GoalLite;
  onEdit: () => void;
  onRequestDelete: () => void;
};

export default function GoalCard({ goal, onEdit, onRequestDelete }: Props) {
  return (
    <div className="border rounded-xl p-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <div className="font-semibold">{goal.title}</div>
            <span className={`text-2xs px-2 py-0.5 rounded ${priClasses[goal.priority]}`}>
              {goal.priority}
            </span>
            <span className="text-2xs text-slate-400">#{goal.goalId}</span>
          </div>
          {goal.description && (
            <div className="text-sm text-slate-600 mt-1">{goal.description}</div>
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
