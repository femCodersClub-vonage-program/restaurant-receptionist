package com.restaurantreceptionist.reservations;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReservationReadResponse(LocalDate date, Sessions sessions) {

	public record Sessions(
			@JsonProperty("LUNCH") SessionReservations lunch,
			@JsonProperty("DINNER") SessionReservations dinner) {
	}

	public record SessionReservations(List<ReservationItem> reservations, Capacity capacity) {
	}

	public record ReservationItem(
			UUID id,
			String customerFirstName,
			String customerLastName,
			String phoneNumber,
			LocalDate reservationDate,
			String arrivalTime,
			short partySize,
			ReservationStatus status) {
	}

	public record Capacity(int totalCapacity, int occupiedCapacity, int availableCapacity) {
	}
}
