// src/api/CategoryApi.ts
import { http } from "./http";

/** Backend DTO */
type CategoryDTO = {
  categoryId: number;
  name: string;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

/** UI model (use this everywhere in the frontend) */
export type Category = {
  id: number;
  name: string;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

/** Request types (exported so components can import) */
export type CreateCategoryRequest = {
  name: string;
  description?: string | null;
};
export type UpdateCategoryRequest = {
  name?: string;
  description?: string | null;
};

const toCategory = (d: CategoryDTO): Category => ({
  id: d.categoryId,
  name: d.name,
  description: d.description ?? null,
  createdAt: d.createdAt,
  updatedAt: d.updatedAt,
});
const toCategoryArray = (arr: CategoryDTO[]): Category[] => arr.map(toCategory);

// make sure this never returns null
const uid = () => (localStorage.getItem("devUserId") ?? "35");

export const CategoriesAPI = {
  async list(): Promise<Category[]> {
    const data = await http<CategoryDTO[]>(`/users/${uid()}/categories`);
    return toCategoryArray(data);
  },

  async create(body: CreateCategoryRequest): Promise<Category> {
    const data = await http<CategoryDTO>(`/users/${uid()}/categories`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    return toCategory(data);
  },

  async update(id: number, body: UpdateCategoryRequest): Promise<Category> {
    const data = await http<CategoryDTO>(`/users/${uid()}/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    });
    return toCategory(data);
  },

  async remove(id: number): Promise<void> {
    await http<void>(`/users/${uid()}/categories/${id}`, { method: "DELETE" });
  },

  async neglected(days: number): Promise<any[]> {
    return http<any[]>(`/users/${uid()}/categories/neglected?days=${days}`);
  },
};

export default CategoriesAPI;
