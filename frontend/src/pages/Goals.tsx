import { useEffect, useState } from "react";

type Goal = {
    goalId: number;
    title: string;
    description: string;
};

export default function Goals() {
    const [goals, setGoals] = useState<Goal[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);



    const fetchGoals = async () => {
        try {
            setLoading(true);
            const response = await fetch("/api/goals"); // update with full URL if needed
            if (!response.ok) throw new Error("Failed to fetch goals");
            const data = await response.json();
            setGoals(data);
        } catch (err: any) {
            setError(err.message || "Something went wrong");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchGoals();
    }, []);

    const completeGoal = async (goalId: number) => {
        try {
            const response = await fetch(`/api/goals/${goalId}/complete`, {
                method: "POST",
            });

            if (!response.ok) {
                throw new Error("Failed to complete goal");
            }

            // Optionally: show toast, or re-fetch goals
            alert("Goal completed! 🏆");

            // Optionally: remove completed goal from list (or refetch)


        } catch (err: any) {
            alert(err.message || "Error completing goal");
        }
    };

    return (
        <section className="space-y-4">
            <h1 className="text-2xl font-bold">Your Goals 🎯</h1>
            <p className="text-gray-600">Mark your goals as completed to earn wins!</p>

            {loading && <div>Loading...</div>}
            {error && <div className="text-red-500">{error}</div>}

            <div className="grid gap-4 md:grid-cols-1">
                {goals.length === 0 && !loading ? (
                    <div className="text-gray-500">No goals found. Set one to get started!</div>
                ) : (
                    goals.map((goal) => (
                        <div
                            key={goal.goalId}
                            className="rounded-xl border bg-white p-4 shadow-sm flex justify-between items-center"
                        >
                            <div>
                                <div className="font-medium">{goal.title}</div>
                                <div className="text-sm text-gray-500">{goal.description}</div>
                            </div>
                            <button
                                onClick={() => completeGoal(goal.goalId)}
                                className="text-green-600 hover:text-green-800 text-xl"
                                title="Mark as complete"
                            >
                                ✅
                            </button>
                        </div>
                    ))
                )}
            </div>
        </section>
    );
}