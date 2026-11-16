package com.restaurantreceptionist.reservations;

import java.time.LocalDate;
import java.time.LocalTime;

public record ConfirmedReservationCommand(
	String firstName,
	String lastName,
	String phoneNumber,
	LocalDate reservationDate,
	LocalTime arrivalTime,
	int partySize) {

	@Override
	public String toString() {
		return "ConfirmedReservationCommand[reservationDate=" + reservationDate
			+ ", arrivalTime=" + arrivalTime
			+ ", partySize=" + partySize
			+ ", personalData=<redacted>]";
	}
}
