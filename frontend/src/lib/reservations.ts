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

const UNREACHABLE_ERROR =
  "The reservation service could not be reached. Please try again.";

async function readErrorDetail(response: Response): Promise<string> {
  try {
    const problem = (await response.json()) as { detail?: unknown };
    if (typeof problem.detail === "string" && problem.detail.length > 0) {
      return problem.detail;
    }
  } catch {
    // Not a Problem Details response; fall through to the status-based message.
  }
  return response.status >= 500 ? UNREACHABLE_ERROR : GENERIC_ERROR;
}

export async function fetchReservationsByDate(
  date: string,
): Promise<ReservationsResponse> {
  let response: Response;
  try {
    response = await fetch(`/api/reservations?date=${encodeURIComponent(date)}`);
  } catch {
    throw new Error(UNREACHABLE_ERROR);
  }

  if (!response.ok) {
    throw new Error(await readErrorDetail(response));
  }

  return (await response.json()) as ReservationsResponse;
}
