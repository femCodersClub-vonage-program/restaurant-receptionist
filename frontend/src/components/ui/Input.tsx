import { forwardRef } from "react";
import type { InputHTMLAttributes } from "react";

import { cn } from "@/lib/cn";

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { error = false, className, ...props },
  ref,
) {
  return (
    <input
      ref={ref}
      {...props}
      className={cn(
        "w-full rounded-control border bg-surface px-3 py-2 text-body text-foreground placeholder:text-foreground-muted",
        error ? "border-error" : "border-border",
        className,
      )}
      aria-invalid={error || undefined}
    />
  );
});
