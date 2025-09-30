import { http } from "./http";
import { getDevUserId } from "../auth/devUser";

// Allowed priority values
export type Priority = "HIGH" | "MEDIUM" | "LOW";

/*
    The following types are shared between frontend and backend.
*/
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

export type CreateGoalRequest = {
  categoryId: number;
  title: string;
  description?: string | null;
  priority: Priority;
};

export type UpdateGoalRequest = Partial<{
  categoryId: number;
  title: string;
  description: string | null;
  priority: Priority;
}>;

const uid = () => getDevUserId();

// List all goals for a user (optionally filter by category)
const list = (opts?: { categoryId?: number }) =>
  http<GoalResponse[]>(
    `/users/${uid()}/goals${opts?.categoryId ? `?categoryId=${opts.categoryId}` : ""}`
  );

// Create a new goal
const create = (body: Required<CreateGoalRequest>) =>
  http<GoalResponse>(`/users/${uid()}/goals`, {
    method: "POST",
    body: JSON.stringify(body),
  });

  
// Update an existing goal
const update = (goalId: number, body: UpdateGoalRequest) =>
  http<GoalResponse>(`/users/${uid()}/goals/${goalId}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });

// Delete a goal
const remove = (goalId: number) =>
  http<void>(`/users/${uid()}/goals/${goalId}`, { method: "DELETE" });

// Grouped listing by priority (HIGH/MEDIUM/LOW). Optional categoryId & priority filter.
const grouped = (opts?: { categoryId?: number; priority?: Priority }) => {
  const qs = new URLSearchParams();
  if (opts?.categoryId) qs.set("categoryId", String(opts.categoryId));
  if (opts?.priority) qs.set("priority", opts.priority);
  const q = qs.toString();
  return http<Record<Priority, GoalResponse[]>>(
    `/users/${uid()}/goals/grouped${q ? `?${q}` : ""}`
  );
};

// API wrapper around backend endpoints.
export const GoalsAPI = {
  list,
  create,
  update,
  remove,
  delete: remove,
  grouped,
};
