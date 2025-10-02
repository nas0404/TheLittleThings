import React from "react";
import  { useEffect, useState } from "react";
import Card from "../components/ui/Card";
import ChallengeCard from "../components/ui/ChallengeCard";
import UserStatsCard from "../components/ui/UserStatsCard";
import FriendsChallengesCard from "../components/ui/FriendsChallengesCard";

export default function Home() {
  return (
    <section className="space-y-6">
      <h1 className="text-2xl font-bold">Welcome</h1>
      <p>Start here. Add widgets or cards that matter to your user.</p>

      <div className="flex flex-col gap-6">
        <Card title="" description="">
          <UserStatsCard username="MaximumTab" streak={7} trophies={120} />
        </Card>

        <Card title="Challenge of the Week" description="">
          <ChallengeCard
            title="Complete 6 workouts"
            description="Stay consistent with your fitness routine."
            reward={20}
            progress={{ current: 3, total: 6 }}
          />
        </Card>

        {/* Card 2 → pulled out into its own component */}
        <FriendsChallengesCard />

        <Card title="Card 3" description="Sign in to access your account." />
      </div>
    </section>
  );
}
