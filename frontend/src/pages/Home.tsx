// Home.tsx
import Card from "../components/ui/Card";
import ChallengeCard from "../components/ui/ChallengeCard";
import UserStatsCard from "../components/ui/UserStatsCard";
import FriendsChallengesCard from "../components/ui/FriendsChallengesCard";
import { useMe } from "../api/users";
import { useEffect, useState } from "react";
import { GoalsAPI, type GoalResponse } from "../api/GoalApi";
import GoalCard from "../components/goals/GoalCard";

export default function Home() {
  const { me, loading, err } = useMe();

  // --- Active goals state (for Card 3) ---
  const [goals, setGoals] = useState<GoalResponse[]>([]);
  const [gLoading, setGLoading] = useState(true);
  const [gErr, setGErr] = useState<string | null>(null);

  async function loadGoals() {
    setGLoading(true);
    setGErr(null);
    try {
      // If your API returns only active goals by default, this is enough.
      // If it returns completed too, filter here if a field like `completed` exists.
      const list = await GoalsAPI.list({});
      // Optional filtering if your response includes a completed flag:
      // const list = (await GoalsAPI.list({})).filter(g => !g.completed);
      setGoals(list);
    } catch (e: any) {
      setGErr(e?.message ?? "Failed to load goals");
    } finally {
      setGLoading(false);
    }
  }

  useEffect(() => {
    loadGoals();
  }, []);

  async function completeGoal(goalId: number) {
    const token = localStorage.getItem("token");
    if (!token) return alert("Please log in");
    try {
      const res = await fetch(`/api/goals/${goalId}/complete`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      });
      if (!res.ok) throw new Error(await res.text());
      // Refresh goals so completed one disappears
      await loadGoals();
      // Optional: refresh home/profile stats immediately
      window.dispatchEvent(new Event("user-updated"));
    } catch (err: any) {
      alert(err?.message ?? "Error completing goal");
    }
  }

  return (
    <section className="space-y-6">
      <h1 className="text-2xl font-bold">Welcome</h1>
      <p>Start here. Add widgets or cards that matter to your user.</p>

      <div className="flex flex-col gap-6">
        <Card title="" description="">
          {loading ? (
            <div className="text-sm text-gray-500">Loading your stats…</div>
          ) : err || !me ? (
            <div className="text-sm text-red-600">{err ?? "Not logged in"}</div>
          ) : (
            <UserStatsCard
              username={me.username}
              streak={me.streaks ?? 0}
              trophies={me.trophies ?? 0}
            />
          )}
        </Card>

        <Card title="Challenge of the Week" description="">
          <ChallengeCard
            title="Complete 6 workouts"
            description="Stay consistent with your fitness routine."
            reward={20}
            progress={{ current: 3, total: 6 }}
          />
        </Card>

        {/* Friends challenges */}
        <FriendsChallengesCard />

        {/* Card 3 → Active Goals */}
        <Card title="Active Goals" description="Your current goals. Complete to turn them into Wins.">
          {gLoading ? (
            <div className="text-sm text-gray-500">Loading goals…</div>
          ) : gErr ? (
            <div className="text-sm text-red-600">{gErr}</div>
          ) : goals.length === 0 ? (
            <div className="text-sm text-gray-500">No active goals. Create one from Add Goal.</div>
          ) : (
            <div className="space-y-3">
              {goals.map((g) => (
                <GoalCard
                  key={g.goalId}
                  goal={{
                    goalId: g.goalId,
                    title: g.title,
                    description: g.description ?? null,
                    priority: g.priority,
                  }}
                  showEditDelete={false}              // HIDE edit/delete here
                  onComplete={() => completeGoal(g.goalId)} // ONLY complete button
                />
              ))}
            </div>
          )}
        </Card>
      </div>
    </section>
  );
}
