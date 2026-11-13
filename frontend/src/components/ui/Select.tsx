import { forwardRef } from "react";
import type { SelectHTMLAttributes } from "react";
import { ChevronDown } from "lucide-react";

import { cn } from "@/lib/cn";

export interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  error?: boolean;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { error = false, className, children, ...props },
  ref,
) {
  return (
    <div className="relative">
      <select
        ref={ref}
        {...props}
        className={cn(
          "w-full appearance-none rounded-control border bg-surface py-2 pl-3 pr-8 text-body text-foreground",
          error ? "border-error" : "border-control-border",
          className,
        )}
        aria-invalid={error || undefined}
      >
        {children}
      </select>
      <ChevronDown
        className="pointer-events-none absolute right-3 top-1/2 size-4 -translate-y-1/2 text-foreground-muted"
        aria-hidden="true"
      />
    </div>
  );
});
