import {
  Alert,
  Badge,
  CapacityIndicator,
  Card,
  Table,
} from "@/components/ui";
import type { TableColumn } from "@/components/ui";
import type { ReservationItem } from "@/lib/reservations";

import { LoadingState } from "./LoadingState";

export type ServiceSectionStatus = "loading" | "success" | "error";

export interface ServiceSectionProps {
  title: string;
  arrivalWindow: string;
  total: number;
  occupied: number;
  available: number;
  reservations: ReservationItem[];
  status: ServiceSectionStatus;
  error?: string;
}

type ReservationRow = {
  id: string;
  name: string;
  phone: string;
  time: string;
  party: number;
  status: ReservationItem["status"];
};

const reservationColumns: TableColumn<ReservationRow>[] = [
  { key: "name", header: "Name", cell: (row) => row.name },
  { key: "phone", header: "Phone", cell: (row) => row.phone },
  { key: "time", header: "Time", cell: (row) => row.time },
  { key: "party", header: "Party", cell: (row) => row.party },
  {
    key: "status",
    header: "Status",
    cell: (row) => (
      <Badge variant={row.status === "CONFIRMED" ? "success" : "neutral"}>
        {row.status === "CONFIRMED" ? "Confirmed" : row.status}
      </Badge>
    ),
  },
];

function toRow(reservation: ReservationItem): ReservationRow {
  return {
    id: reservation.id,
    name: [reservation.customerFirstName, reservation.customerLastName]
      .filter(Boolean)
      .join(" "),
    phone: reservation.phoneNumber,
    time: reservation.arrivalTime,
    party: reservation.partySize,
    status: reservation.status,
  };
}

export function ServiceSection({
  title,
  arrivalWindow,
  total,
  occupied,
  available,
  reservations,
  status,
  error,
}: ServiceSectionProps) {
  const headingId = `${title.toLowerCase()}-heading`;
  const rows = reservations.map(toRow);

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
              rows={rows}
              rowKey={(row) => row.id}
              emptyMessage={`No ${title.toLowerCase()} reservations for this date.`}
            />
          )}
        </div>
      </Card>
    </section>
  );
}
