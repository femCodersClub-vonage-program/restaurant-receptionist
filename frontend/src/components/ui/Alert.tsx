import type { ReactNode } from "react";
import { CircleAlert, CircleCheck, TriangleAlert } from "lucide-react";

import { cn } from "@/lib/cn";

export type AlertVariant = "success" | "warning" | "error";

export interface AlertProps {
  variant: AlertVariant;
  title?: string;
  children: ReactNode;
  className?: string;
}

const variants: Record<
  AlertVariant,
  { classes: string; role: "status" | "alert"; Icon: typeof CircleCheck }
> = {
  success: {
    classes: "bg-success-surface text-success",
    role: "status",
    Icon: CircleCheck,
  },
  warning: {
    classes: "bg-warning-surface text-warning",
    role: "status",
    Icon: TriangleAlert,
  },
  error: {
    classes: "bg-error-surface text-error",
    role: "alert",
    Icon: CircleAlert,
  },
};

export function Alert({ variant, title, children, className }: AlertProps) {
  const { classes, role, Icon } = variants[variant];

  return (
    <div
      role={role}
      className={cn(
        "flex gap-3 rounded-surface p-4 text-body",
        classes,
        className,
      )}
    >
      <Icon className="size-4 shrink-0" aria-hidden="true" />
      <div className="flex flex-col gap-1">
        {title && <p className="text-label">{title}</p>}
        <p>{children}</p>
      </div>
    </div>
  );
}
