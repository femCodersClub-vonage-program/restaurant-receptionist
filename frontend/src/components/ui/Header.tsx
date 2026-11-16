import type { ReactNode } from "react";

import { cn } from "@/lib/cn";

export interface HeaderProps {
  title: string;
  description?: ReactNode;
  actions?: ReactNode;
  className?: string;
}

export function Header({
  title,
  description,
  actions,
  className,
}: HeaderProps) {
  return (
    <header className={cn("flex flex-col gap-4", className)}>
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div className="flex flex-col gap-2">
          <h1 className="text-heading-lg">{title}</h1>
          {description && (
            <p className="text-foreground-muted">{description}</p>
          )}
        </div>
        {actions && (
          <div className="flex flex-wrap items-center gap-3">{actions}</div>
        )}
      </div>
    </header>
  );
}
