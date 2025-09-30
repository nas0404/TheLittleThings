import { http } from "./http";

type CategoryDTO = {
  categoryId: number;
  name: string;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

export type Category = {
  id: number;
  name: string;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

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

export const CategoriesAPI = {
  async list(): Promise<Category[]> {
    const data = await http<CategoryDTO[]>(`/api/categories`);
    return toCategoryArray(data);
  },

  async create(body: CreateCategoryRequest): Promise<Category> {
    const data = await http<CategoryDTO>(`/api/categories`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    return toCategory(data);
  },

  async update(id: number, body: UpdateCategoryRequest): Promise<Category> {
    const data = await http<CategoryDTO>(`/api/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    });
    return toCategory(data);
  },

  async remove(id: number): Promise<void> {
    await http<void>(`/api/categories/${id}`, { method: "DELETE" });
  },

  async neglected(days: number): Promise<any[]> {
    return http<any[]>(`/api/categories/neglected?days=${days}`);
  },
};

export default CategoriesAPI;

