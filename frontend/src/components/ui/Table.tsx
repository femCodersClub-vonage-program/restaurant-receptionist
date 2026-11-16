import type { ReactNode } from "react";

import { cn } from "@/lib/cn";

export interface TableColumn<T> {
  key: string;
  header: ReactNode;
  cell: (row: T) => ReactNode;
  className?: string;
}

export interface TableProps<T> {
  caption?: string;
  columns: TableColumn<T>[];
  rows: T[];
  rowKey: (row: T) => string;
  emptyMessage: string;
  className?: string;
}

export function Table<T>({
  caption,
  columns,
  rows,
  rowKey,
  emptyMessage,
  className,
}: TableProps<T>) {
  return (
    <div className={cn("overflow-x-auto", className)}>
      <table className="w-full border-collapse text-body">
        {caption && <caption className="sr-only">{caption}</caption>}
        <thead>
          <tr className="border-b border-border">
            {columns.map((column) => (
              <th
                key={column.key}
                scope="col"
                className={cn(
                  "px-4 py-3 text-left text-label text-foreground-muted",
                  column.className,
                )}
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length}
                className="px-4 py-6 text-center text-foreground-muted"
              >
                {emptyMessage}
              </td>
            </tr>
          ) : (
            rows.map((row) => (
              <tr key={rowKey(row)} className="border-b border-border">
                {columns.map((column) => (
                  <td
                    key={column.key}
                    className={cn("px-4 py-3 text-foreground", column.className)}
                  >
                    {column.cell(row)}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
