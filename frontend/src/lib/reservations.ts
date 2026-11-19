export type DiningSessionKey = "LUNCH" | "DINNER";

export interface ReservationItem {
  id: string;
  customerFirstName: string;
  customerLastName: string;
  phoneNumber: string;
  reservationDate: string;
  arrivalTime: string;
  partySize: number;
  status: string;
}

export interface CapacitySummary {
  totalCapacity: number;
  occupiedCapacity: number;
  availableCapacity: number;
}

export interface SessionData {
  reservations: ReservationItem[];
  capacity: CapacitySummary;
}

export interface ReservationsResponse {
  date: string;
  sessions: Record<DiningSessionKey, SessionData>;
}

const GENERIC_ERROR =
  "Reservation data could not be loaded. Please try again.";

export async function fetchReservationsByDate(
  date: string,
): Promise<ReservationsResponse> {
  const response = await fetch(`/api/reservations?date=${encodeURIComponent(date)}`);

  if (!response.ok) {
    let detail = GENERIC_ERROR;
    try {
      const problem = (await response.json()) as { detail?: unknown };
      if (typeof problem.detail === "string" && problem.detail.length > 0) {
        detail = problem.detail;
      }
    } catch {
      detail = GENERIC_ERROR;
    }
    throw new Error(detail);
  }

  return (await response.json()) as ReservationsResponse;
}
