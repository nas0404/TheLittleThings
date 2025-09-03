import { useState } from "react";

export default function Leaderboard() {
    const [region, setRegion] = useState("");
    const regions = ["Europe", "Asia", "America"]; // Static list for now

    // Placeholder user list
    const users = [
        { username: "Alice", trophies: 1500, region: "Europe" },
        { username: "Bob", trophies: 1300, region: "Asia" },
        { username: "Charlie", trophies: 1200, region: "America" },
    ];

    return (
        <section className="space-y-4">
            <h1 className="text-2xl font-bold">Leaderboard</h1>

            <div>
                <label className="mr-2">Filter by Region:</label>
                <select
                    value={region}
                    onChange={(e) => setRegion(e.target.value)}
                    className="border px-2 py-1 rounded"
                >
                    <option value="">All Regions</option>
                    {regions.map((r) => (
                        <option key={r} value={r}>
                            {r}
                        </option>
                    ))}
                </select>
            </div>

            <div className="grid gap-4 md:grid-cols-1">
                {users
                    .filter((user) => !region || user.region === region)
                    .map((user, index) => (
                        <div
                            key={user.username}
                            className="rounded-xl border bg-white p-4 shadow-sm flex justify-between items-center"
                        >
                            <div>
                                <span className="font-bold">#{index + 1}</span>{" "}
                                <span>{user.username}</span>
                            </div>
                            <div>
                                🏆 {user.trophies} | 🌍 {user.region}
                            </div>
                        </div>
                    ))}
            </div>
        </section>
    );
}