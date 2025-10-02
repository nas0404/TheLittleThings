// src/pages/FriendsPage.tsx
import React, { useEffect, useState } from "react";
import { FriendsAPI, type Friendship, type Challenge } from "../api/friends";
import Card from "../components/ui/Card"; // <- update path if needed

type Toast = { kind: "ok" | "err"; msg: string };

const useFriendsData = () => {
  const [friends, setFriends] = useState<Friendship[]>([]);
  const [incoming, setIncoming] = useState<Friendship[]>([]);
  const [proposed, setProposed] = useState<Challenge[]>([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState<Toast | null>(null);

  const refresh = async () => {
    setLoading(true);
    try {
      const [f, inc, pc] = await Promise.all([
        FriendsAPI.listFriends(),
        FriendsAPI.listIncoming(),
        FriendsAPI.listProposedChallenges(), // <- new
      ]);
      setFriends(f);
      setIncoming(inc);
      setProposed(pc);
    } catch (e: any) {
      setToast({ kind: "err", msg: e.message || "Failed to load friends" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  return { friends, setFriends, incoming, setIncoming, proposed, setProposed, loading, toast, setToast, refresh };
};

const FriendsPage: React.FC = () => {
  const { friends, setFriends, incoming, setIncoming, proposed, setProposed, loading, toast, setToast, refresh } =
    useFriendsData();

  // Add by username
  const [addName, setAddName] = useState<string>("");

  const onAdd = async () => {
    const name = addName.trim();
    if (!name) return setToast({ kind: "err", msg: "Enter a username" });
    try {
      const res = await FriendsAPI.sendRequestByUsername(name);
      setToast({ kind: "ok", msg: `Request sent to ${res.friendUsername}` });
      setAddName("");
    } catch (e: any) {
      setToast({ kind: "err", msg: e.message || "Failed to send request" });
    }
  };

  // Friend requests
  const onAccept = async (otherUserId: number) => {
    try {
      const res = await FriendsAPI.acceptRequest(otherUserId);
      setIncoming(prev => prev.filter(p => p.friendId !== otherUserId));
      setFriends(prev => [...prev, res]);
      setToast({ kind: "ok", msg: `You are now friends with ${res.friendUsername}` });
    } catch (e: any) {
      setToast({ kind: "err", msg: e.message });
    }
  };

  const onDecline = async (otherUserId: number) => {
    try {
      await FriendsAPI.declineRequest(otherUserId);
      setIncoming(prev => prev.filter(p => p.friendId !== otherUserId));
      setToast({ kind: "ok", msg: "Request declined" });
    } catch (e: any) {
      setToast({ kind: "err", msg: e.message });
    }
  };

  const onRemove = async (friendUserId: number) => {
    const snapshot = friends;
    setFriends(prev => prev.filter(f => f.friendId !== friendUserId));
    try {
      await FriendsAPI.removeFriend(friendUserId);
      setToast({ kind: "ok", msg: "Friend removed" });
    } catch (e: any) {
      setFriends(snapshot);
      setToast({ kind: "err", msg: e.message });
    }
  };

  // Challenge invites (for me as opponent)
  const onAcceptChallenge = async (id: number) => {
    try {
      await FriendsAPI.acceptChallenge(id);
      setProposed(p => p.filter(x => x.id !== id));
      setToast({ kind: "ok", msg: "Challenge accepted" });
    } catch (e: any) {
      setToast({ kind: "err", msg: e.message || "Failed to accept challenge" });
    }
  };

  const onDeclineChallenge = async (id: number) => {
    try {
      await FriendsAPI.declineChallenge(id);
      setProposed(p => p.filter(x => x.id !== id));
      setToast({ kind: "ok", msg: "Challenge declined" });
    } catch (e: any) {
      setToast({ kind: "err", msg: e.message || "Failed to decline challenge" });
    }
  };

  return (
    <div className="mx-auto max-w-5xl px-4 py-6 space-y-8">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Friends</h1>
        <button
          className="rounded-xl border px-3 py-2 text-sm hover:bg-gray-50"
          onClick={refresh}
          disabled={loading}
        >
          Refresh
        </button>
      </header>

      {/* Add friend */}
      <Card title="Add a friend" description="Send a friend request by username.">
        <div className="flex flex-col gap-3 sm:flex-row">
          <input
            type="text"
            placeholder="Friend username"
            className="w-full rounded-xl border px-3 py-2"
            value={addName}
            onChange={e => setAddName(e.target.value)}
            autoComplete="off"
          />
          <button
            onClick={onAdd}
            className="rounded-xl bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
          >
            Send request
          </button>
        </div>
        <p className="mt-2 text-sm text-gray-500">Usernames are case-insensitive.</p>
      </Card>

      {/* Challenge invites (I am the opponent) */}
      <Card
        title="Challenge invites"
        description="Challenges proposed to you by friends. Accept to start or decline to dismiss."
      >
        {proposed.length === 0 ? (
          <p className="text-sm text-gray-500">No challenge invites.</p>
        ) : (
          <ul className="space-y-3">
            {proposed.map(ch => (
              <li key={ch.id} className="flex items-center justify-between rounded-xl border p-3">
                <div>
                  <div className="font-medium">From user #{ch.challengerId}</div>
                  <div className="text-sm text-gray-700">{ch.goalList}</div>
                  <div className="text-xs text-gray-500">Stake: {ch.trophiesStake}</div>
                </div>
                <div className="flex gap-2">
                  <button
                    className="rounded-lg bg-green-600 px-3 py-1.5 text-white hover:bg-green-700"
                    onClick={() => onAcceptChallenge(ch.id)}
                  >
                    Accept
                  </button>
                  <button
                    className="rounded-lg bg-gray-200 px-3 py-1.5 hover:bg-gray-300"
                    onClick={() => onDeclineChallenge(ch.id)}
                  >
                    Decline
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </Card>

      {/* Incoming friend requests */}
      <Card
        title="Incoming requests"
        description="Friend requests waiting for your response."
      >
        {incoming.length === 0 ? (
          <p className="text-sm text-gray-500">No pending requests.</p>
        ) : (
          <ul className="space-y-3">
            {incoming.map(req => (
              <li key={req.id} className="flex items-center justify-between rounded-xl border p-3">
                <div>
                  <div className="font-medium">{req.friendUsername}</div>
                  <div className="text-xs text-gray-500">
                    Requested at {new Date(req.requestedAt).toLocaleString()}
                  </div>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => onAccept(req.friendId)}
                    className="rounded-lg bg-green-600 px-3 py-1.5 text-white hover:bg-green-700"
                  >
                    Accept
                  </button>
                  <button
                    onClick={() => onDecline(req.friendId)}
                    className="rounded-lg bg-gray-200 px-3 py-1.5 hover:bg-gray-300"
                  >
                    Decline
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </Card>

      {/* Friends list */}
      <Card title="Your friends" description="Challenge your friends or remove them.">
        {friends.length === 0 ? (
          <p className="text-sm text-gray-500">No friends yet.</p>
        ) : (
          <ul className="grid gap-3 sm:grid-cols-2">
            {friends.map(fr => (
              <FriendCard key={fr.friendId} f={fr} onRemove={onRemove} />
            ))}
          </ul>
        )}
      </Card>

      {/* toast */}
      {toast && (
        <div
          className={`fixed bottom-6 right-6 rounded-xl px-4 py-3 text-sm shadow-lg ${
            toast.kind === "ok" ? "bg-green-600 text-white" : "bg-red-600 text-white"
          }`}
          onAnimationEnd={() => setTimeout(() => setToast(null), 2500)}
        >
          {toast.msg}
        </div>
      )}
    </div>
  );
};

export default FriendsPage;

const FriendCard: React.FC<{
  f: Friendship;
  onRemove: (friendUserId: number) => void;
}> = ({ f, onRemove }) => {
  const [open, setOpen] = useState(false); // challenge dialog
  return (
    <li className="rounded-2xl border p-4">
      <div className="flex items-center justify-between">
        <div>
          <div className="font-medium">{f.friendUsername}</div>
          <div className="text-xs text-gray-500">Friend ID: {f.friendId}</div>
        </div>
        <div className="flex gap-2">
          <button
            className="rounded-lg bg-indigo-600 px-3 py-1.5 text-white hover:bg-indigo-700"
            onClick={() => setOpen(true)}
          >
            Challenge
          </button>
          <button
            className="rounded-lg bg-gray-200 px-3 py-1.5 hover:bg-gray-300"
            onClick={() => onRemove(f.friendId)}
          >
            Remove
          </button>
        </div>
      </div>
      {open && <ChallengeDialog friendId={f.friendId} friendName={f.friendUsername} onClose={() => setOpen(false)} />}
    </li>
  );
};

const ChallengeDialog: React.FC<{
  friendId: number;
  friendName: string;
  onClose: () => void;
}> = ({ friendId, friendName, onClose }) => {
  const [goalList, setGoalList] = useState("Complete 6 workouts");
  const [startDate, setStartDate] = useState<string>("");
  const [endDate, setEndDate] = useState<string>("");
  const [stake, setStake] = useState<number>(0);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const submit = async () => {
    setBusy(true);
    setErr(null);
    try {
      await FriendsAPI.createChallenge({
        opponentId: friendId,
        goalList,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        trophiesStake: stake || 0,
      });
      onClose();
    } catch (e: any) {
      setErr(e.message || "Failed to create challenge");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-black/40 p-4">
      <div className="w-full max-w-lg rounded-2xl bg-white p-5">
        <h3 className="mb-4 text-lg font-semibold">Challenge {friendName}</h3>
        <div className="space-y-3">
          <label className="block">
            <span className="text-sm text-gray-600">Goal(s)</span>
            <textarea
              className="mt-1 w-full rounded-xl border px-3 py-2"
              rows={3}
              value={goalList}
              onChange={(e) => setGoalList(e.target.value)}
            />
          </label>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label className="block">
              <span className="text-sm text-gray-600">Start date</span>
              <input
                type="date"
                className="mt-1 w-full rounded-xl border px-3 py-2"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
            </label>
            <label className="block">
              <span className="text-sm text-gray-600">End date</span>
              <input
                type="date"
                className="mt-1 w-full rounded-xl border px-3 py-2"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </label>
          </div>
          <label className="block">
            <span className="text-sm text-gray-600">Trophies stake</span>
            <input
              type="number"
              min={0}
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={stake}
              onChange={(e) => setStake(parseInt(e.target.value || "0", 10))}
            />
          </label>
          {err && <div className="text-sm text-red-600">{err}</div>}
          <div className="flex justify-end gap-2 pt-2">
            <button className="rounded-xl px-3 py-2 hover:bg-gray-100" onClick={onClose} disabled={busy}>
              Cancel
            </button>
            <button
              className="rounded-xl bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-700 disabled:opacity-60"
              onClick={submit}
              disabled={busy || !goalList.trim()}
            >
              Create challenge
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
