import type { JSX } from "react";
import { useNavigate } from "react-router-dom";

type Category = {
  categoryId: number; 
  userId: number;       
  name: string;            
  description?: string;     
  createdAt?: string;       
  updatedAt?: string;       
};
const CATEGORIES: Category[] = [
  { categoryId: 1, userId: 100, name: "Fitness",  description: "Health & training" },
  { categoryId: 2, userId: 100, name: "Career",   description: "Work & growth" },
  { categoryId: 3, userId: 101, name: "Learning", description: "Study & skills" },
];
export default function CategoriesIndexPage(): JSX.Element {
  const navigate = useNavigate();

  return (
    <div className="p-6">
      <h1 className="mb-4 text-xl font-semibold">Categories</h1>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {CATEGORIES.map((c) => (
          <button
            key={c.categoryId}
            onClick={() => navigate(`/categories/${c.categoryId}`)}
            className="rounded-xl border bg-white p-4 text-left hover:shadow transition"
          >
            <div className="mb-1 font-medium">{c.name}</div>
            <div className="text-sm text-gray-600">
              {c.description ?? "No description"}
            </div>
            <div className="mt-2 text-xs text-gray-400">User: {c.userId}</div>
          </button>
        ))}
      </div>
    </div>
  );
}