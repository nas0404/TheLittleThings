import React, { useEffect, useState } from "react";
import Card from "../ui/Card";
import ChallengeCard from "../ui/ChallengeCard";
import { FriendsAPI, type Challenge } from "../../api/friends";

export default function FriendsChallengesCard() {
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [invites, setInvites] = useState<Challenge[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const me = localStorage.getItem("username") || "";

  const load = async () => {
    setLoading(true);
    setErr(null);
    try {
      const [mine, proposedToMe] = await Promise.all([
        FriendsAPI.listMyChallenges(),
        FriendsAPI.listProposedChallenges(),
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

  const requestComplete = async (id: number) => {
    try { await FriendsAPI.requestComplete(id); await load(); }
    catch (e: any) { setErr(e.message || "Failed to request completion"); }
  };
  const confirmComplete = async (id: number) => {
    try { await FriendsAPI.confirmComplete(id); await load(); }
    catch (e: any) { setErr(e.message || "Failed to confirm"); }
  };
  const rejectComplete = async (id: number) => {
    try { await FriendsAPI.rejectComplete(id); await load(); }
    catch (e: any) { setErr(e.message || "Failed to reject"); }
  };

  const renderOne = (c: Challenge) => {
    const partner = c.challengerUsername === me ? c.opponentUsername : c.challengerUsername;

    const requestedByPartner =
        c.status === "completion_requested" &&
        c.completionRequestedByUsername &&
        c.completionRequestedByUsername !== me;

    const requestedByMe =
        c.status === "completion_requested" &&
        c.completionRequestedByUsername === me;

    return (
        <div key={c.id}>
        <ChallengeCard
            title={c.goalList || "Challenge"}
            description={[
            `With ${partner}`,
            c.startDate && c.endDate ? `• ${c.startDate} → ${c.endDate}` : null,
            `• Status: ${c.status}`,
            ].filter(Boolean).join(" ")}
            reward={c.trophiesStake ?? 0}
            progress={{ current: c.status === "completed" ? 1 : 0, total: 1 }}
        />

        {/* Actions */}
        <div className="mt-2 flex flex-wrap gap-2">
            {(c.status === "accepted" || c.status === "active") && (
            <button
                className="rounded-md bg-indigo-600 px-3 py-1.5 text-white hover:bg-indigo-700"
                onClick={async () => {
                try { await FriendsAPI.requestComplete(c.id); await load(); }
                catch (e: any) { setErr(e.message || "Failed to request completion"); }
                }}
            >
                Mark complete
            </button>
            )}

            {requestedByPartner && (
            <>
                <button
                className="rounded-md bg-green-600 px-3 py-1.5 text-white hover:bg-green-700"
                onClick={async () => {
                    try { await FriendsAPI.confirmComplete(c.id); await load(); }
                    catch (e: any) { setErr(e.message || "Failed to acknowledge"); }
                }}
                >
                Acknowledge
                </button>
                <button
                className="rounded-md bg-gray-200 px-3 py-1.5 hover:bg-gray-300"
                onClick={async () => {
                    try { await FriendsAPI.rejectComplete(c.id); await load(); }
                    catch (e: any) { setErr(e.message || "Failed to reject"); }
                }}
                >
                Reject
                </button>
            </>
            )}

            {requestedByMe && (
            <span className="text-sm text-gray-600">Waiting for {partner}…</span>
            )}
        </div>
        </div>
    );
    };

  return (
    <Card
      title="Your challenges with friends"
      description="Active and proposed challenges you’re involved in."
    >
      {loading && <p className="text-sm text-gray-500">Loading challenges…</p>}
      {err && <p className="text-sm text-red-600">{err}</p>}

      {!loading && !err && challenges.length === 0 && invites.length === 0 && (
        <p className="text-sm text-gray-500">No challenges yet.</p>
      )}

      {/* Mine (accepted/active/proposed-by-me) */}
      <div className="grid gap-4 md:grid-cols-2">
        {challenges.map(renderOne)}
      </div>

      {/* Invites for me */}
      {invites.length > 0 && (
        <>
          <h4 className="mt-6 text-sm font-semibold text-gray-700">Invites for you</h4>
          <div className="mt-2 grid gap-4 md:grid-cols-2">
            {invites.map(renderOne)}
          </div>
        </>
      )}
    </Card>
  );
}
