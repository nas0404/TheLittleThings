import { useState } from "react";

type Win = {
    id: number;
    goal: string;
    date: string;
};
export default function Wins() {
    // Static list of wins for now Imma implement it
    const [wins] = useState<Win[]>([
        {
            id: 1,
            goal: "Complete 5 daily challenges",
            date: "2025-09-01",
        },
        {
            id: 2,
            goal: "Win a weekly tournament",
            date: "2025-08-28",
        },
        {
            id: 3,
            goal: "Reach 1000 trophies",
            date: "2025-08-25",
        },
    ]);

    return (
        <section className="space-y-4">
            <h1 className="text-2xl font-bold">Your Wins 🏆</h1>
            <p className="text-gray-600">These are the goals you've completed successfully.</p>

            <div className="grid gap-4 md:grid-cols-1">
                {wins.length === 0 ? (
                    <div className="text-gray-500">No wins yet. Keep going!</div>
                ) : (
                    wins.map((win) => (
                        <div
                            key={win.id}
                            className="rounded-xl border bg-white p-4 shadow-sm flex justify-between items-center"
                        >
                            <div>
                                <div className="font-medium">{win.goal}</div>
                                <div className="text-sm text-gray-500">
                                    Completed on: {new Date(win.date).toLocaleDateString()}
                                </div>
                            </div>
                            <div className="text-2xl">✅</div>
                        </div>
                    ))
                )}
            </div>
        </section>
    );
}