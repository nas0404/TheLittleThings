import * as React from "react";
import { Link } from "react-router-dom";
import Card from "../ui/Card";
import { CategoriesAPI } from "../../api/CategoryApi";

type NeglectedItem = {
  categoryId: number;
  name: string;
  lastWinAt?: string | null;
  neglectDays?: number | null;
};

const MIN_DAYS = 1;
const MAX_DAYS = 3650;
const DEBOUNCE_MS = 400;


export default function CategoryNeglected() {
  const [daysInput, setDaysInput] = React.useState("14");
  const parsedDays = React.useMemo(() => parseInt(daysInput, 10), [daysInput]);
  const isValid =
    Number.isFinite(parsedDays) &&
    parsedDays >= MIN_DAYS &&
    parsedDays <= MAX_DAYS;

  const [items, setItems] = React.useState<NeglectedItem[]>([]);
  const [loading, setLoading] = React.useState(false);
  const [err, setErr] = React.useState<string | null>(null);

  const clampOnBlur = () => {
    if (!Number.isFinite(parsedDays)) {
      setDaysInput(String(MIN_DAYS));
      return;
    }
    const clamped = Math.min(Math.max(parsedDays, MIN_DAYS), MAX_DAYS);
    setDaysInput(String(clamped));
  };

  React.useEffect(() => {
    if (!isValid) return;

    let alive = true;
    setLoading(true);
    setErr(null);

    const t = setTimeout(async () => {
      try {
        const data = await CategoriesAPI.neglected(parsedDays);
        if (alive) setItems(data as NeglectedItem[]);
      } catch (e: any) {
        if (alive) {
          setErr(e?.message || "Failed to load neglected categories");
          setItems([]);
        }
      } finally {
        if (alive) setLoading(false);
      }
    }, DEBOUNCE_MS);

    return () => {
      alive = false;
      clearTimeout(t);
    };
  }, [parsedDays, isValid]);

  const fmtDate = (iso?: string | null) =>
    iso ? new Date(iso).toLocaleDateString() : "no wins yet";

  return (
    <Card
      title="Neglected categories"
      description="No wins within the lookback window; ordered by oldest last activity."
      className="h-fit"
    >
      <div className="mb-2 flex items-center gap-2">
        <label className="text-sm" htmlFor="neglected-days">
          Lookback days
        </label>
        <input
          id="neglected-days"
          type="number"
          inputMode="numeric"
          min={MIN_DAYS}
          max={MAX_DAYS}
          value={daysInput}
          onChange={(e) => setDaysInput(e.target.value)}
          onBlur={clampOnBlur}
          className={`w-24 rounded border px-2 py-1 text-sm ${
            isValid ? "" : "border-red-400"
          }`}
          placeholder="14"
          aria-invalid={!isValid}
        />
      </div>
      {!isValid && (
        <div className="mb-2 text-sm text-red-600">
          Enter a number between {MIN_DAYS} and {MAX_DAYS}.
        </div>
      )}

      {loading && <div className="text-sm text-gray-500">Loading…</div>}
      {err && <div className="text-sm text-red-600">{err}</div>}

      {!loading && !err && isValid && (
        <ul className="space-y-2">
          {items.length === 0 ? (
            <div className="text-sm text-gray-500">
              Nothing is neglected (last {parsedDays} days).
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
