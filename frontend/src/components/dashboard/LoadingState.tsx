import { LoaderCircle } from "lucide-react";

export interface LoadingStateProps {
  message?: string;
}

export function LoadingState({
  message = "Loading reservations…",
}: LoadingStateProps) {
  return (
    <div
      role="status"
      aria-live="polite"
      className="flex items-center gap-3 rounded-surface border border-border bg-surface p-6 text-foreground-muted"
    >
      <LoaderCircle
        className="size-4 shrink-0 animate-spin motion-reduce:animate-none"
        aria-hidden="true"
      />
      <p>{message}</p>
    </div>
  );
}
