// src/pages/Home.tsx
import React, { useEffect, useState } from "react";
import Card from "../components/ui/Card";
import ChallengeCard from "../components/ui/ChallengeCard";
import UserStatsCard from "../components/ui/UserStatsCard";
import { FriendsAPI, type Challenge } from "../api/friends";

export default function Home() {
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [invites, setInvites] = useState<Challenge[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setErr(null);
    try {
      // Fetch:
      // - challenges I’m involved in (active/accepted/proposed I created)
      // - invites sent to me (status PROPOSED where I am opponent)
      const [mine, proposedToMe] = await Promise.all([
        FriendsAPI.listMyChallenges(),          // see API helper below
        FriendsAPI.listProposedChallenges(),    // already implemented earlier
      ]);
      setChallenges(mine);
      setInvites(proposedToMe);
    } catch (e: any) {
      setErr(e.message || "Failed to load challenges");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const renderChallenge = (c: Challenge) => {
    const me = localStorage.getItem("username"); 
    const partner =
      c.challengerUsername === me ? c.opponentUsername : c.challengerUsername;

    const title = c.goalList;
    const desc = [
      `With ${partner}`,
      c.startDate && c.endDate ? `• ${c.startDate} → ${c.endDate}` : null,
      `• Status: ${c.status}`,
    ]
      .filter(Boolean)
      .join(" ");

    return (
      <ChallengeCard
        key={c.id}
        title={title}
        description={desc}
        reward={c.trophiesStake ?? 0}
        progress={{ current: c.status === "completed" ? 1 : 0, total: 1 }}
      />
    );
  };


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

        {/* Card 2 → Friends challenges */}
        <Card
          title="Your challenges with friends"
          description="Active and proposed challenges you’re involved in."
        >
          {loading && <p className="text-sm text-gray-500">Loading challenges…</p>}
          {err && <p className="text-sm text-red-600">{err}</p>}

          {!loading && !err && challenges.length === 0 && invites.length === 0 && (
            <p className="text-sm text-gray-500">No challenges yet.</p>
          )}

          {/* Active / accepted / proposed by me */}
          <div className="grid gap-4 md:grid-cols-2">
            {challenges.map(renderChallenge)}
          </div>

          {/* Invites (proposed to me) */}
          {invites.length > 0 && (
            <>
              <h4 className="mt-6 text-sm font-semibold text-gray-700">Invites for you</h4>
              <div className="mt-2 grid gap-4 md:grid-cols-2">
                {invites.map(renderChallenge)}
              </div>
            </>
          )}
        </Card>

        <Card title="Card 3" description="Sign in to access your account." />
      </div>
    </section>
  );
}
