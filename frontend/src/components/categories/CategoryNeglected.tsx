import * as React from "react";
import { Link } from "react-router-dom";
import Card from "../ui/Card";
import { CategoriesAPI } from "../../api/CategoryApi";

// Backend view shape (loose/compatible)
type NeglectedItem = {
  categoryId: number;
  name: string;
  lastWinAt?: string | null;  // may be null/undefined when never had a win
  neglectDays?: number | null; // optional if your query returns it
};

export default function CategoryNeglected() {
  // Lookback window (Option B semantics on backend)
  const [days, setDays] = React.useState<number>(14);

  const [items, setItems] = React.useState<NeglectedItem[]>([]);
  const [loading, setLoading] = React.useState<boolean>(true);
  const [err, setErr] = React.useState<string | null>(null);

  React.useEffect(() => {
    let alive = true;
    (async () => {
      try {
        setLoading(true);
        setErr(null);
        const data = await CategoriesAPI.neglected(days);
        if (alive) setItems(data as NeglectedItem[]);
      } catch (e: any) {
        if (alive) setErr(e?.message || "Failed to load neglected categories");
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => {
      alive = false;
    };
  }, [days]);

  const onDaysChange: React.ChangeEventHandler<HTMLInputElement> = (e) => {
    const n = Number(e.target.value);
    setDays(Number.isFinite(n) && n > 0 ? Math.floor(n) : 1);
  };

  const fmtDate = (iso?: string | null) =>
    iso ? new Date(iso).toLocaleDateString() : "no wins yet";

  return (
    <Card
      title="Neglected categories"
      description="No wins within the lookback window; ordered by oldest last activity."
      className="h-fit"
    >
      <div className="mb-3 flex items-center gap-2">
        <label className="text-sm">Lookback days</label>
        <input
          type="number"
          min={1}
          value={days}
          onChange={onDaysChange}
          className="w-24 rounded border px-2 py-1 text-sm"
        />
      </div>

      {loading && <div className="text-sm text-gray-500">Loading…</div>}
      {err && <div className="text-sm text-red-600">{err}</div>}

      {!loading && !err && (
        <ul className="space-y-2">
          {items.length === 0 ? (
            <div className="text-sm text-gray-500">
              Nothing is neglected (last {days} days).
            </div>
          ) : (
            items.map((x) => (
              <li
                key={x.categoryId}
                className="flex items-center justify-between rounded-lg border px-3 py-2 hover:bg-gray-50"
              >
                <Link
                  to={`/categories/${x.categoryId}`}
                  className="truncate hover:underline"
                  title={`Go to ${x.name}`}
                >
                  {x.name}
                </Link>
                <span className="text-xs text-gray-500">
                  {typeof x?.neglectDays === "number" && x.neglectDays >= 0
                    ? `${x.neglectDays}d ago`
                    : fmtDate(x.lastWinAt)}
                </span>
              </li>
            ))
          )}
        </ul>
      )}
    </Card>
  );
}
