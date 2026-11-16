import { cn } from "@/lib/cn";

export interface CapacityIndicatorProps {
  total: number;
  occupied: number;
  available: number;
  label?: string;
  className?: string;
}

export function CapacityIndicator({
  total,
  occupied,
  available,
  label,
  className,
}: CapacityIndicatorProps) {
  const ratio = total > 0 ? Math.min(Math.max(occupied / total, 0), 1) : 0;
  const width = `${Math.round(ratio * 100)}%`;

  return (
    <div className={cn("flex flex-col gap-3", className)}>
      {label && <p className="text-label text-foreground-muted">{label}</p>}
      <div className="flex flex-col gap-2">
        <div className="flex flex-col gap-1">
          <p className="text-heading-sm">
            {available} of {total} places available
          </p>
          <p className="text-foreground-muted">{occupied} places occupied</p>
        </div>
        <div
          aria-hidden="true"
          className="h-2 w-full overflow-hidden rounded-control bg-selection"
        >
          <div
            className="h-full rounded-control bg-primary"
            style={{ width }}
          />
        </div>
      </div>
    </div>
  );
}
