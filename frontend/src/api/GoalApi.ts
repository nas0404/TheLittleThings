import { http } from "./http";

// Type representing the priority levels of a goal
export type Priority = "HIGH" | "MEDIUM" | "LOW";

// Type representing the API response structure for a goal
export type GoalResponse = {
  goalId: number;
  userId: number;
  categoryId: number;
  title: string;
  description?: string | null;
  priority: Priority;
  createdAt: string;
  updatedAt: string;
};

// Type representing the request structure for creating a new goal
export type CreateGoalRequest = {
  categoryId: number;
  title: string;
  description?: string | null;
  priority: Priority;
};

// Type representing the request structure for updating an existing goal
export type UpdateGoalRequest = Partial<{
  categoryId: number;
  title: string;
  description: string | null;
  priority: Priority;
}>;

// Fetch list of goals, optionally filtered by category
const list = (opts?: { categoryId?: number }) =>
  http<GoalResponse[]>(`/goals${opts?.categoryId ? `?categoryId=${opts.categoryId}` : ""}`);

// Create a new goal
const create = (body: CreateGoalRequest) =>
  http<GoalResponse>(`/goals`, {
    method: "POST",
    body: JSON.stringify(body),
  });

// Update an existing goal by ID
const update = (goalId: number, body: UpdateGoalRequest) =>
  http<GoalResponse>(`/goals/${goalId}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });

// Delete a goal by ID
const remove = (goalId: number) =>
  http<void>(`/goals/${goalId}`, { method: "DELETE" });

// Fetch goals grouped by priority, optionally filtered by category and priority
const grouped = (opts?: { categoryId?: number; priority?: Priority }) => {
  const qs = new URLSearchParams();
  if (opts?.categoryId) qs.set("categoryId", String(opts.categoryId));
  if (opts?.priority) qs.set("priority", opts.priority);
  const q = qs.toString();
  return http<Record<Priority, GoalResponse[]>>(`/goals/grouped${q ? `?${q}` : ""}`);
};

export const GoalsAPI = { list, create, update, remove, grouped };
