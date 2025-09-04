import { Trophy } from "lucide-react";

type ChallengeCardProps = {
  title: string;
  description: string;
  reward: number;
  progress: { current: number; total: number };
};

export default function ChallengeCard({
  title,
  description,
  reward,
  progress,
}: ChallengeCardProps) {
  const percentage = (progress.current / progress.total) * 100;

  return (
    <div className="rounded-xl border bg-white p-6 shadow-sm">
      {/* Top row: title + reward */}
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold">{title}</h3>

        <div className="flex items-center gap-2 text-sm font-medium text-gray-700">
          <span>Reward:</span>
          <Trophy className="h-5 w-5 text-yellow-500" />
          <span>{reward}</span>
        </div>
      </div>

      {/* Description */}
      <p className="mt-2 text-sm text-gray-600">{description}</p>

      {/* Progress */}
      <div className="mt-4">
        <div className="mb-1 text-sm font-medium text-gray-700">
          {progress.current}/{progress.total} completed
        </div>
        <div className="h-2 w-full rounded-full bg-gray-200">
          <div
            className="h-2 rounded-full bg-blue-600 transition-all"
            style={{ width: `${percentage}%` }}
          />
        </div>
      </div>
    </div>
  );
}
