// // src/api/categories.ts
// // ---------------------
// // Wrapper for all Category API calls.
// // Handles grabbing the current userId from /api/users/me,
// // and exposes clean functions for list, create, update, delete, neglected.

// import { authHeaders } from "../auth/token";

// // --- Backend DTO (what Spring Boot sends) ---
// export type CategoryDTO = {
//   categoryId: number;
//   userId: number;
//   name: string;
//   description?: string | null;
//   createdAt?: string; // ISO string
//   updatedAt?: string; // ISO string
// };

// // --- Requests ---
// export type CreateCategoryRequest = {
//   name: string;
//   description?: string | null;
// };

// export type UpdateCategoryRequest = {
//   name?: string;
//   description?: string | null;
// };

// // --- Frontend-facing type ---
// // Our React components will use this cleaned-up shape.
// export type Category = {
//   id: number;
//   userId: number;
//   name: string;
//   description?: string | null;
//   createdAt?: string;
//   updatedAt?: string;
// };

// // Convert backend DTO → frontend Category
// export function toCategory(dto: CategoryDTO): Category {
//   return {
//     id: dto.categoryId,
//     userId: dto.userId,
//     name: dto.name,
//     description: dto.description ?? undefined,
//     createdAt: dto.createdAt,
//     updatedAt: dto.updatedAt,
//   };
// }

// // --- Helper: fetch current userId once and cache it ---
// let cachedUserId: number | null = null;
// async function getCurrentUserId(): Promise<number> {
//   if (cachedUserId != null) return cachedUserId;

//   const res = await fetch("/api/users/me", { headers: { ...authHeaders() } });
//   if (!res.ok) throw new Error("Not authenticated (failed to fetch /me)");
//   const json = await res.json(); // expect { userId, username, ... }

//   cachedUserId = json.userId;
//   return cachedUserId!;
// }

// // --- Categories API object ---
// export const CategoriesAPI = {
//   // List all categories for current user
//   async list(): Promise<Category[]> {
//     const uid = await getCurrentUserId();
//     const res = await fetch(`/api/users/${uid}/categories`, {
//       headers: { "Content-Type": "application/json", ...authHeaders() },
//     });
//     if (!res.ok) throw new Error(await res.text().catch(() => "Failed to load categories"));
//     const data = (await res.json()) as CategoryDTO[];
//     return data.map(toCategory);
//   },

//   // Create a new category
//   async create(body: CreateCategoryRequest): Promise<Category> {
//     const uid = await getCurrentUserId();
//     const res = await fetch(`/api/users/${uid}/categories`, {
//       method: "POST",
//       headers: { "Content-Type": "application/json", ...authHeaders() },
//       body: JSON.stringify(body),
//     });
//     if (!res.ok) throw new Error(await res.text().catch(() => "Failed to create category"));
//     const data = (await res.json()) as CategoryDTO;
//     return toCategory(data);
//   },

//   // Update an existing category
//   async update(categoryId: number, body: UpdateCategoryRequest): Promise<Category> {
//     const uid = await getCurrentUserId();
//     const res = await fetch(`/api/users/${uid}/categories/${categoryId}`, {
//       method: "PUT",
//       headers: { "Content-Type": "application/json", ...authHeaders() },
//       body: JSON.stringify(body),
//     });
//     if (!res.ok) throw new Error(await res.text().catch(() => "Failed to update category"));
//     const data = (await res.json()) as CategoryDTO;
//     return toCategory(data);
//   },

//   // Delete a category
//   async remove(categoryId: number): Promise<void> {
//     const uid = await getCurrentUserId();
//     const res = await fetch(`/api/users/${uid}/categories/${categoryId}`, {
//       method: "DELETE",
//       headers: { ...authHeaders() },
//     });
//     if (!res.ok) throw new Error(await res.text().catch(() => "Failed to delete category"));
//   },

//   // Get neglected categories (e.g. no wins in X days)
//   async neglected(days = 14): Promise<
//     { categoryId: number; name: string; lastWinAt?: string | null }[]
//   > {
//     const uid = await getCurrentUserId();
//     const url = `/api/users/${uid}/categories/neglected?days=${encodeURIComponent(days)}`;
//     const res = await fetch(url, { headers: { ...authHeaders() } });
//     if (!res.ok) throw new Error(await res.text().catch(() => "Failed to load neglected categories"));
//     return (await res.json()) as {
//       categoryId: number;
//       name: string;
//       lastWinAt?: string | null;
//     }[];
//   },
// };
// src/api/CategoryApi.ts
import { authHeaders } from "../auth/token";
import { getDevUserId } from "../auth/devUser";

export type CategoryDTO = {
  categoryId: number;
  userId: number;
  name: string;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

export type Category = {
  id: number;
  userId: number;
  name: string;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

export function toCategory(dto: CategoryDTO): Category {
  return {
    id: dto.categoryId,
    userId: dto.userId,
    name: dto.name,
    description: dto.description ?? undefined,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  };
}

let cachedUserId: number | null = null;

async function getCurrentUserId(): Promise<number> {
  // 1) dev override if present
  const devId = getDevUserId();
  if (devId) {
    cachedUserId = devId;
    return devId;
  }

  // 2) otherwise try real /me (requires token)
  try {
    const res = await fetch("/api/users/me", { headers: { ...authHeaders() } });
    if (res.status === 401) {
      throw new Error("No dev user set and not authenticated. Go to /dev-user or log in.");
    }
    if (!res.ok) throw new Error(`/me failed: ${res.status}`);
    const me = await res.json();
    cachedUserId = me.userId;
    return cachedUserId!;
  } catch (err: any) {
    // nicer message when backend is down
    if ((err?.message || "").includes("Failed to fetch")) {
      throw new Error("Backend unreachable. Start Spring Boot or use /dev-user.");
    }
    throw err;
  }
}


export const CategoriesAPI = {
  async list() {
    const uid = await getCurrentUserId();
    const res = await fetch(`/api/users/${uid}/categories`, {
      headers: { "Content-Type": "application/json", ...authHeaders() },
    });
    if (!res.ok) throw new Error(await res.text().catch(() => "Failed to load categories"));
    return (await res.json() as CategoryDTO[]).map(toCategory);
  },
  async create(body: { name: string; description?: string | null }) {
    const uid = await getCurrentUserId();
    const res = await fetch(`/api/users/${uid}/categories`, {
      method: "POST",
      headers: { "Content-Type": "application/json", ...authHeaders() },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(await res.text().catch(() => "Failed to create category"));
    return toCategory(await res.json());
  },
  async update(categoryId: number, body: { name?: string; description?: string | null }) {
    const uid = await getCurrentUserId();
    const res = await fetch(`/api/users/${uid}/categories/${categoryId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json", ...authHeaders() },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(await res.text().catch(() => "Failed to update category"));
    return toCategory(await res.json());
  },
  async remove(categoryId: number) {
    const uid = await getCurrentUserId();
    const res = await fetch(`/api/users/${uid}/categories/${categoryId}`, {
      method: "DELETE",
      headers: { ...authHeaders() },
    });
    if (!res.ok) throw new Error(await res.text().catch(() => "Failed to delete category"));
  },
  async neglected(days = 14) {
    const uid = await getCurrentUserId();
    const res = await fetch(`/api/users/${uid}/categories/neglected?days=${encodeURIComponent(days)}`, {
      headers: { ...authHeaders() },
    });
    if (!res.ok) throw new Error(await res.text().catch(() => "Failed to load neglected categories"));
    return await res.json() as { categoryId: number; name: string; lastWinAt?: string | null }[];
  },
};
