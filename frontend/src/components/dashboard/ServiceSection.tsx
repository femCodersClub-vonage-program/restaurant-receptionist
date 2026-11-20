import {
  Alert,
  Badge,
  CapacityIndicator,
  Card,
  Table,
} from "@/components/ui";
import type { BadgeVariant, TableColumn } from "@/components/ui";
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

const STATUS_PRESENTATION: Record<
  string,
  { label: string; variant: BadgeVariant }
> = {
  CONFIRMED: { label: "Confirmed", variant: "success" },
};

function statusPresentation(status: string): {
  label: string;
  variant: BadgeVariant;
} {
  return STATUS_PRESENTATION[status] ?? { label: status, variant: "neutral" };
}

const reservationColumns: TableColumn<ReservationRow>[] = [
  { key: "name", header: "Name", cell: (row) => row.name },
  { key: "phone", header: "Phone", cell: (row) => row.phone },
  { key: "time", header: "Time", cell: (row) => row.time },
  { key: "party", header: "Party", cell: (row) => row.party },
  {
    key: "status",
    header: "Status",
    cell: (row) => {
      const { label, variant } = statusPresentation(row.status);
      return <Badge variant={variant}>{label}</Badge>;
    },
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

          {status === "loading" ? (
            <LoadingState
              message={`Loading ${title.toLowerCase()} reservations…`}
            />
          ) : status === "error" ? (
            <Alert variant="error" title="Could not load reservations">
              {error ?? "Reservation data could not be loaded. Please try again."}
            </Alert>
          ) : (
            <>
              <CapacityIndicator
                label={title}
                total={total}
                occupied={occupied}
                available={available}
              />
              <Table
                caption={`${title} reservations`}
                columns={reservationColumns}
                rows={rows}
                rowKey={(row) => row.id}
                emptyMessage={`No ${title.toLowerCase()} reservations for this date.`}
              />
            </>
          )}
        </div>
      </Card>
    </section>
  );
}
