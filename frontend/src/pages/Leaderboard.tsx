import { useState, useEffect } from "react";

interface LeaderboardUser {
    userId: number;
    username: string;
    region: string;
    trophies: number;
}

export default function Leaderboard() {
    const [region, setRegion] = useState("");
    const [users, setUsers] = useState<LeaderboardUser[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const regions = ["Europe", "Asia", "America", "Australia", "Friends"];

    useEffect(() => {
        async function fetchLeaderboard() {
            setLoading(true);
            setError(null);

            try {
                let url = "http://localhost:8080/api/leaderboard";
                if (region) {
                    url += `?region=${encodeURIComponent(region)}`;
                }

                const response = await fetch(url);
                if (!response.ok) {
                    throw new Error("Network response was not ok");
                }
                const data: LeaderboardUser[] = await response.json();
                setUsers(data);
            } catch (err) {
                setError("Failed to fetch leaderboard data");
            } finally {
                setLoading(false);
            }
        }

        fetchLeaderboard();
    }, [region]);

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

            {loading && <p>Loading leaderboard...</p>}
            {error && <p className="text-red-500">{error}</p>}

            {!loading && !error && (
                <div className="overflow-x-auto rounded-xl border bg-white">
                    <div className="min-w-full divide-y divide-gray-200">
                        {/* Header Row */}
                        <div className="grid grid-cols-3 font-semibold text-gray-700 bg-gray-100 px-4 py-2">
                            <div>Rank</div>
                            <div>Username</div>
                            <div className="text-right">ඞ🏆 Trophies</div>
                        </div>

                        {/* User Rows */}
                        {users.map((user, index) => (
                            <div
                                key={user.userId}
                                className="grid grid-cols-3 px-4 py-2 items-center hover:bg-gray-50 transition"
                            >
                                <div>#{index + 1}</div>
                                <div>{user.username}</div>
                                <div className="text-right">
                                    {user.trophies}{" "}
                                    <span className="text-sm text-gray-400">({user.region})</span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </section>
    );
}