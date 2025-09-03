import { cn } from "../../lib/utils";

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  isLoading?: boolean;
};

export default function Button({ children, isLoading, className, ...props }: ButtonProps) {
  return (
    <button
      {...props}
      disabled={isLoading || props.disabled}
      className={cn(
        "inline-flex items-center justify-center rounded-lg px-4 py-2 font-medium shadow-sm transition disabled:opacity-50",
        className || "bg-blue-600 text-white hover:bg-blue-700"
      )}
    >
      {isLoading ? (
        <span className="animate-spin mr-2 h-4 w-4 rounded-full border-2 border-white border-t-transparent"></span>
      ) : null}
      {children}
    </button>
  );
}
