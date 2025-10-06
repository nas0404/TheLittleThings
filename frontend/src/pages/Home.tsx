
import Card from "../components/ui/Card";
import ChallengeCard from "../components/ui/ChallengeCard";
import UserStatsCard from "../components/ui/UserStatsCard";
import FriendsChallengesCard from "../components/ui/FriendsChallengesCard"; 
import { useMe } from "../api/users";

export default function Home() {
  const { me, loading, err } = useMe();

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

        {/* Card 2 → friends challenges */}
        <FriendsChallengesCard />

        <Card title="Card 3" description="Sign in to access your account." />
      </div>
    </section>
  );
}
