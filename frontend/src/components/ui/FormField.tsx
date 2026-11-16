import { useId } from "react";
import type { ReactNode } from "react";

import { cn } from "@/lib/cn";

export interface FormFieldControlProps {
  id: string;
  "aria-describedby": string | undefined;
  "aria-invalid": true | undefined;
  "aria-required": true | undefined;
  error: boolean;
}

export interface FormFieldProps {
  label: string;
  required?: boolean;
  helpText?: string;
  error?: string;
  className?: string;
  children: (controlProps: FormFieldControlProps) => ReactNode;
}

export function FormField({
  label,
  required = false,
  helpText,
  error,
  className,
  children,
}: FormFieldProps) {
  const id = useId();
  const helpId = `${id}-help`;
  const errorId = `${id}-error`;
  const describedBy = error ? errorId : helpText ? helpId : undefined;

  return (
    <div className={cn("flex flex-col gap-2", className)}>
      <label htmlFor={id} className="text-label">
        {label}
        {required && (
          <span aria-hidden="true" className="text-error">
            {" *"}
          </span>
        )}
      </label>
      {children({
        id,
        "aria-describedby": describedBy,
        "aria-invalid": error ? true : undefined,
        "aria-required": required || undefined,
        error: Boolean(error),
      })}
      {error ? (
        <p id={errorId} className="text-label text-error">
          {error}
        </p>
      ) : helpText ? (
        <p id={helpId} className="text-label text-foreground-muted">
          {helpText}
        </p>
      ) : null}
    </div>
  );
}
