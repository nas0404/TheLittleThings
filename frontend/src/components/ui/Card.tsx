
import * as React from "react";
import { cn } from "../../lib/utils";

type CardProps = {
  title: string;
  description: string;
  children?: React.ReactNode;
  className?: string;
};

export default function Card({ title, description, children, className }: CardProps) {
  return (
    <div
      className={cn(
        "rounded-xl border bg-white p-6 shadow-md transition hover:shadow-lg",
        className
      )}
    >
      <h2 className="text-lg font-semibold">{title}</h2>
      <p className="mt-2 text-sm text-gray-600">{description}</p>
      {children ? <div className="mt-4">{children}</div> : null}
    </div>
  );
}
