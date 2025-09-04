// src/components/home/UserStatsCard.tsx
import { Flame, Trophy } from "lucide-react";

type Props = {
  username: string;
  streak: number;
  trophies: number;
};

export default function UserStatsCard({ username, streak, trophies }: Props) {
  return (
    <div className="grid grid-cols-3 items-center w-full">
      {/* Left: username */}
      <span className="justify-self-start text-lg font-semibold">
        {username}
      </span>

      {/* Middle: streak with flame icon */}
      <div className="justify-self-center flex items-center gap-2">
        <Flame className="h-5 w-5 text-orange-500" aria-hidden />
        <span className="font-medium">{streak}</span>
      </div>

      {/* Right: trophies */}
      <div className="justify-self-end flex items-center gap-2">
        <Trophy className="h-5 w-5 text-yellow-500" aria-hidden />
        <span className="font-medium">{trophies}</span>
      </div>
    </div>
  );
}
