

export type Friendship = {
  id: number;
  friendId: number;
  friendUsername: string;
  status: "pending" | "accepted" | "declined" | "canceled" | "blocked";
  outgoing: boolean;
  requestedAt: string;
};

export type Challenge = {
  id: number;
  goalList: string;
  trophiesStake: number;
  status: "proposed" | "accepted" | "declined" | "active" | "completion_requested" | "completed" | "expired";
  startDate?: string; endDate?: string;

  challengerId: number; challengerUsername: string;
  opponentId: number;   opponentUsername: string;

  completionRequestedById?: number | null;
  completionRequestedByUsername?: string | null;
  completionRequestedAt?: string | null;
};



const BASE = "http://localhost:8080";
const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem("token") || ""}`,
  "Content-Type": "application/json",
});

export const FriendsAPI = {
  // friendships
  listFriends: async (): Promise<Friendship[]> => {
    const r = await fetch(`${BASE}/api/friends`, { headers: authHeader() });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },

  listIncoming: async (): Promise<Friendship[]> => {
    const r = await fetch(`${BASE}/api/friends/requests/incoming`, { headers: authHeader() });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },

  sendRequest: async (targetUserId: number): Promise<Friendship> => {
    const r = await fetch(`${BASE}/api/friends/requests`, {
      method: "POST",
      headers: authHeader(),
      body: JSON.stringify({ targetUserId }),
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },

  acceptRequest: async (otherUserId: number): Promise<Friendship> => {
    const r = await fetch(`${BASE}/api/friends/requests/${otherUserId}/accept`, {
      method: "POST",
      headers: authHeader(),
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },

  declineRequest: async (otherUserId: number): Promise<Friendship> => {
    const r = await fetch(`${BASE}/api/friends/requests/${otherUserId}/decline`, {
      method: "POST",
      headers: authHeader(),
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },

  cancelRequest: async (otherUserId: number): Promise<Friendship> => {
    const r = await fetch(`${BASE}/api/friends/requests/${otherUserId}/cancel`, {
      method: "POST",
      headers: authHeader(),
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },

  removeFriend: async (friendUserId: number): Promise<void> => {
    const r = await fetch(`${BASE}/api/friends/${friendUserId}`, {
      method: "DELETE",
      headers: authHeader(),
    });
    if (!r.ok) throw new Error(await r.text());
  },

  sendRequestByUsername: async (username: string) => {
    const r = await fetch(`http://localhost:8080/api/friends/requests/by-username`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token") || ""}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ username }),
    });
    if (!r.ok) {
      const msg = await r.text().catch(() => "");
      throw new Error(`${r.status} ${r.statusText}${msg ? ` – ${msg}` : ""}`);
    }
    return r.json();
  },

  searchUsers: async (q: string) => {
    const r = await fetch(`${BASE}/api/users/search?q=${encodeURIComponent(q)}&limit=10`, {
      headers: authHeader(),
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json() as Promise<Array<{ userId: number; username: string }>>;
  },

  // challenges ----------------------------------------------------------------------------------
  createChallenge: async (payload: {
    opponentId: number;
    goalList: string;
    startDate?: string; // yyyy-mm-dd
    endDate?: string;
    trophiesStake?: number;
  }): Promise<Challenge> => {
    const r = await fetch(`${BASE}/api/friends/challenges`, {
      method: "POST",
      headers: authHeader(),
      body: JSON.stringify(payload),
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },

  listProposedChallenges: async (): Promise<Challenge[]> => {
    const r = await fetch(`http://localhost:8080/api/friends/challenges/proposed`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token") || ""}`,
      },
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },

  listMyChallenges: async (): Promise<Challenge[]> => {
    const r = await fetch(`${BASE}/api/friends/challenges/mine`, { headers: authHeader() });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },

  acceptChallenge: async (id: number): Promise<Challenge> => {
    const r = await fetch(`http://localhost:8080/api/friends/challenges/${id}/accept`, {
      method: "POST",
      headers: { Authorization: `Bearer ${localStorage.getItem("token") || ""}` },
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },
  declineChallenge: async (id: number): Promise<Challenge> => {
    const r = await fetch(`http://localhost:8080/api/friends/challenges/${id}/decline`, {
      method: "POST",
      headers: { Authorization: `Bearer ${localStorage.getItem("token") || ""}` },
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },


  completeChallenge: async (id: number, winnerUserId: number): Promise<Challenge> => {
    const r = await fetch(`${BASE}/api/friends/challenges/${id}/complete?winnerUserId=${winnerUserId}`, {
      method: "POST",
      headers: authHeader(),
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },

  requestComplete: async (id: number) => {
    const r = await fetch(`http://localhost:8080/api/friends/challenges/${id}/request-complete`, {
      method: "POST",
      headers: { Authorization: `Bearer ${localStorage.getItem("token") || ""}` },
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },
  confirmComplete: async (id: number) => {
    const r = await fetch(`http://localhost:8080/api/friends/challenges/${id}/confirm-complete`, {
      method: "POST",
      headers: { Authorization: `Bearer ${localStorage.getItem("token") || ""}` },
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },
  rejectComplete: async (id: number) => {
    const r = await fetch(`http://localhost:8080/api/friends/challenges/${id}/reject-complete`, {
      method: "POST",
      headers: { Authorization: `Bearer ${localStorage.getItem("token") || ""}` },
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
  },
  
};
