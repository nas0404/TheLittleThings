import { useState } from "react";

export default function Leaderboard() {
    const [region, setRegion] = useState("");
    const regions = ["Europe", "Asia", "America", "Australia", "Friends"];

    const users = [
        { username: "Alice", trophies: 1515, region: "Europe" },
        { username: "Bob", trophies: 1300, region: "Asia" },
        { username: "Charlie", trophies: 1220, region: "America" },
        { username: "Alex", trophies: 900, region: "Australia" },
        { username: "Maxim", trophies: 1000, region: "Europe" },
        { username: "Jonathan", trophies: 1405, region: "Asia" },
        { username: "Rose", trophies: 1097, region: "America" },
    ];

    // users and regions are just prototype to show to david on thursday

    const filteredUsers = users
        .filter((user) => !region || user.region === region)
        .sort((a, b) => b.trophies - a.trophies); // Sort by trophies desc

    return (
        <section className="space-y-4">
            <h1 className="text-2xl font-bold">Leaderboard</h1>

            <div>
                <label className="mr-2">Filter by:</label>
                <select
                    value={region}
                    onChange={(e) => setRegion(e.target.value)}
                    className="border px-2 py-1 rounded"
                >
                    <option value="">Global</option>
                    {regions.map((r) => (
                        <option key={r} value={r}>
                            {r}
                        </option>
                    ))}
                </select>
            </div>

            {/* Style of the leaderboard might change later */}
            <div className="overflow-x-auto rounded-xl border bg-white">
                <div className="min-w-full divide-y divide-gray-200">
                    {/* Header Row */}
                    <div className="grid grid-cols-3 font-semibold text-gray-700 bg-gray-100 px-4 py-2">
                        <div>Rank</div>
                        <div>Username</div>
                        <div className="text-right">ඞ🏆 Trophies</div>
                    </div>

                    {/* User Rows */}
                    {filteredUsers.map((user, index) => (
                        <div
                            key={user.username}
                            className="grid grid-cols-3 px-4 py-2 items-center hover:bg-gray-50 transition"
                        >
                            <div>#{index + 1}</div>
                            <div>{user.username}</div>
                            <div className="text-right">
                                {user.trophies} <span className="text-sm text-gray-400">({user.region})</span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
}