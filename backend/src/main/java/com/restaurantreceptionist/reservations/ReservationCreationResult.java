package com.restaurantreceptionist.reservations;

import com.restaurantreceptionist.availability.CapacitySummary;

import java.util.UUID;

public sealed interface ReservationCreationResult {

	record Created(UUID reservationId, DiningSession diningSession) implements ReservationCreationResult {
	}

	record InvalidData(ReservationField field, Reason reason) implements ReservationCreationResult {

		public enum Reason {
			MISSING,
			INVALID_FORMAT,
			PAST_DATE,
			TIME_NOT_IN_FUTURE,
			OUTSIDE_ARRIVAL_WINDOW,
			OUT_OF_RANGE
		}
	}

	record InsufficientCapacity(
		CapacitySummary capacity,
		int requestedPartySize
	) implements ReservationCreationResult {
	}
}
