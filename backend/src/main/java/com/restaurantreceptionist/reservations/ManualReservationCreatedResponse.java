package com.restaurantreceptionist.reservations;

import java.util.UUID;

import com.restaurantreceptionist.reservations.ReservationCreationResult.Created;

public record ManualReservationCreatedResponse(UUID id, DiningSession diningSession) {

	static ManualReservationCreatedResponse from(Created created) {
		return new ManualReservationCreatedResponse(created.reservationId(), created.diningSession());
	}
}
