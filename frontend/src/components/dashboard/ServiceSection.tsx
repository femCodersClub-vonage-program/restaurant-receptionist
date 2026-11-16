import {
  Alert,
  CapacityIndicator,
  Card,
  Table,
} from "@/components/ui";
import type { TableColumn } from "@/components/ui";

import { LoadingState } from "./LoadingState";

export type ServiceSectionStatus = "loading" | "empty" | "error";

export interface ServiceSectionProps {
  title: string;
  arrivalWindow: string;
  total: number;
  occupied: number;
  available: number;
  status: ServiceSectionStatus;
  error?: string;
}

type ReservationRow = {
  id: string;
  name: string;
  time: string;
  party: string;
  status: string;
};

const reservationColumns: TableColumn<ReservationRow>[] = [
  { key: "name", header: "Name", cell: (row) => row.name },
  { key: "time", header: "Time", cell: (row) => row.time },
  { key: "party", header: "Party", cell: (row) => row.party },
  { key: "status", header: "Status", cell: (row) => row.status },
];

export function ServiceSection({
  title,
  arrivalWindow,
  total,
  occupied,
  available,
  status,
  error,
}: ServiceSectionProps) {
  const headingId = `${title.toLowerCase()}-heading`;

  return (
    <section aria-labelledby={headingId} className="flex flex-col gap-4">
      <Card className="p-6">
        <div className="flex flex-col gap-6">
          <div className="flex flex-col gap-1">
            <h2 id={headingId} className="text-heading-md">
              {title}
            </h2>
            <p className="text-foreground-muted">
              Arrival window {arrivalWindow}
            </p>
          </div>

          <CapacityIndicator
            label={title}
            total={total}
            occupied={occupied}
            available={available}
          />

          {status === "loading" ? (
            <LoadingState
              message={`Loading ${title.toLowerCase()} reservations…`}
            />
          ) : status === "error" ? (
            <Alert variant="error" title="Could not load reservations">
              {error ?? "Reservation data could not be loaded. Please try again."}
            </Alert>
          ) : (
            <Table
              caption={`${title} reservations`}
              columns={reservationColumns}
              rows={[]}
              rowKey={(row) => row.id}
              emptyMessage="No reservations to show yet. Reservation data will appear here once it is connected."
            />
          )}
        </div>
      </Card>
    </section>
  );
}
