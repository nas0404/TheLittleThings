import { useEffect, useState } from "react";

type Win = {
    winId: number;
    userId: number;
    goalId: number;
    title: string;
    description: string;
    numTrophies: number;
    completionDate: string;
    journalId?: number;
};

type UpdateWinRequest = {
    title?: string;
    description?: string;
    numTrophies?: number;
};

export default function Wins() {
    const [wins, setWins] = useState<Win[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [editForm, setEditForm] = useState<Partial<Win>>({});
    localStorage.setItem("userId", "1");
    const userId = localStorage.getItem("userId");

    useEffect(() => {
        if (!userId) return;
        fetch(`http://localhost:8080/api/wins?userId=${userId}`)
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch wins");
                return res.json();
            })
            .then(setWins)
            .catch((err) => setError(err.message));
    }, [userId]);

    const startEditing = (win: Win) => {
        setEditingId(win.winId);
        setEditForm({ ...win });
    };

    const cancelEditing = () => {
        setEditingId(null);
        setEditForm({});
    };

    const handleInputChange = (field: keyof Win, value: any) => {
        setEditForm((prev) => ({ ...prev, [field]: value }));
    };

    const saveEdit = async (winId: number) => {
        if (!userId) return;
        try {
            const response = await fetch(`http://localhost:8080/api/wins/${winId}?userId=${userId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    title: editForm.title,
                    description: editForm.description,
                    numTrophies: editForm.numTrophies,
                } as UpdateWinRequest),
            });

            if (!response.ok) throw new Error("Failed to update win");

            const updatedWin: Win = await response.json();

            setWins((prev) =>
                prev.map((w) => (w.winId === winId ? updatedWin : w))
            );
            cancelEditing();
        } catch (err) {
            console.error(err);
            setError("Failed to update win");
        }
    };

    const deleteWin = async (winId: number) => {
        if (!userId) return;
        if (!window.confirm("Are you sure you want to delete this win?")) return;

        try {
            const response = await fetch(`http://localhost:8080/api/wins/${winId}?userId=${userId}`, {
                method: "DELETE",
            });

            if (!response.ok) throw new Error("Failed to delete win");

            setWins((prev) => prev.filter((w) => w.winId !== winId));
        } catch (err) {
            console.error(err);
            setError("Failed to delete win");
        }
    };

    return (
        <section className="space-y-4">
            <h1 className="text-2xl font-bold">Your Wins 🏆</h1>
            <p className="text-gray-600">These are the goals you've completed successfully.</p>

            {error && <div className="text-red-500">{error}</div>}

            <div className="grid gap-4 md:grid-cols-1">
                {wins.length === 0 ? (
                    <div className="text-gray-500">No wins yet. Keep going!</div>
                ) : (
                    wins.map((win) => (
                        <div
                            key={win.winId}
                            className="rounded-xl border bg-white p-4 shadow-sm space-y-2"
                        >
                            {editingId === win.winId ? (
                                <div className="space-y-2">
                                    <input
                                        className="w-full border p-2"
                                        value={editForm.title || ""}
                                        onChange={(e) => handleInputChange("title", e.target.value)}
                                    />
                                    <textarea
                                        className="w-full border p-2"
                                        value={editForm.description || ""}
                                        onChange={(e) => handleInputChange("description", e.target.value)}
                                    />
                                    <input
                                        type="number"
                                        className="w-full border p-2"
                                        value={editForm.numTrophies || 0}
                                        onChange={(e) =>
                                            handleInputChange("numTrophies", parseInt(e.target.value))
                                        }
                                    />
                                    <div className="flex gap-2">
                                        <button
                                            onClick={() => saveEdit(win.winId)}
                                            className="bg-blue-500 text-white px-3 py-1 rounded"
                                        >
                                            Save
                                        </button>
                                        <button
                                            onClick={cancelEditing}
                                            className="bg-gray-300 px-3 py-1 rounded"
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <div className="flex justify-between items-start">
                                    <div>
                                        <div className="font-medium">{win.title}</div>
                                        <div className="text-sm text-gray-500">
                                            Completed on:{" "}
                                            {new Date(win.completionDate).toLocaleDateString()}
                                        </div>
                                        {win.description && (
                                            <div className="text-sm mt-1 text-gray-600">
                                                {win.description}
                                            </div>
                                        )}
                                        <div className="text-yellow-600 text-sm font-semibold mt-1">
                                            🏆ඞ Trophies: {win.numTrophies}
                                        </div>
                                    </div>
                                    <div className="flex flex-col items-end space-y-1">
                                        <button
                                            onClick={() => startEditing(win)}
                                            className="text-blue-600 text-sm"
                                        >
                                            Edit
                                        </button>
                                        <button
                                            onClick={() => deleteWin(win.winId)}
                                            className="text-red-600 text-sm"
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    ))
                )}
            </div>
        </section>
    );
}
