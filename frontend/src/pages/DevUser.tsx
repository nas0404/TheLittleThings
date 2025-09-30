import * as React from "react";
import { setDevUserId, getDevUserId, clearDevUserId } from "../auth/devUser";

export default function DevUser() {
  const [value, setValue] = React.useState<string>(String(getDevUserId() ?? ""));
  const current = getDevUserId();

  return (
    <div className="max-w-md mx-auto space-y-4">
      <h1 className="text-2xl font-bold">Dev User</h1>
      <p className="text-sm text-gray-600">
        Temporary override for development. Enter an existing <code>userId</code> from your DB.
      </p>

      <div>
        <label className="block text-sm mb-1">User ID</label>
        <input
          type="number"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          className="w-full rounded-md border px-3 py-2"
          placeholder="e.g. 1"
        />
      </div>

      <div className="flex gap-2">
        <button
          className="rounded-md bg-black text-white px-4 py-2"
          onClick={() => {
            const n = Number(value);
            if (!Number.isFinite(n) || n <= 0) {
              alert("Enter a positive integer user id");
              return;
            }
            setDevUserId(n);
            alert(`Dev user set to ${n}`);
          }}
        >
          Save
        </button>
        <button
          className="rounded-md bg-gray-100 px-4 py-2"
          onClick={() => {
            clearDevUserId();
            setValue("");
            alert("Cleared dev user id");
          }}
        >
          Clear
        </button>
        <a href="/categories" className="ml-auto underline text-blue-600">Go to Categories →</a>
      </div>

      <div className="text-sm text-gray-700">
        Current: {current ?? "none"}
      </div>
    </div>
  );
}
