import { Link, useParams } from "react-router-dom";

// 🔹 Define the Goal type
type Priority = "High" | "Medium" | "Low";

type Goal = {
  goalId: number;
  categoryId: number;
  title: string;
  priority: Priority;
  dueDate?: string | null;
};

const GOALS_BY_CATEGORY: Record<number, Goal[]> = {
  1: [
    { goalId: 11, categoryId: 1, title: "Run 5km under 25min", priority: "High", dueDate: "2025-09-20" },
    { goalId: 12, categoryId: 1, title: "Bench 80kg x 5", priority: "Medium", dueDate: null },
  ],
  2: [
    { goalId: 21, categoryId: 2, title: "Ship React feature", priority: "High", dueDate: "2025-09-05" },
  ],
  3: [
    { goalId: 31, categoryId: 3, title: "Read 20 pages/day", priority: "Low", dueDate: null },
  ],
};

export default function CategoryGoalsPage() {
  const { categoryId } = useParams(); // string | undefined
  const id = Number(categoryId);
  const goals = Number.isFinite(id) ? (GOALS_BY_CATEGORY[id] ?? []) : [];

  if (!Number.isFinite(id)) {
    return <div className="p-6 text-red-600">Invalid category id.</div>;
  }

  return (
    <div className="p-6">
      <div className="mb-3">
        <Link to="/categories" className="rounded border px-2 py-1 text-sm">
          ← Categories
        </Link>
      </div>

      <h1 className="text-xl font-semibold mb-4">Goals for category #{id}</h1>

      {goals.length === 0 ? (
        <div className="rounded-xl border bg-white p-8 text-center text-gray-600">
          No goals yet. (Placeholder)
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {goals.map((g) => (
            <div key={g.goalId} className="rounded-xl border bg-white p-4">
              <div className="mb-1 flex items-start justify-between">
                <div className="font-medium">{g.title}</div>
                <span
                  className={`rounded px-2 py-0.5 text-xs ${g.priority === "High"
                      ? "bg-red-100 text-red-700"
                      : g.priority === "Medium"
                        ? "bg-yellow-100 text-yellow-700"
                        : "bg-green-100 text-green-700"
                    }`}
                >
                  {g.priority}
                </span>
              </div>
              <div className="text-xs text-gray-600">
                {g.dueDate ? new Date(g.dueDate).toLocaleDateString() : "No due date"}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
