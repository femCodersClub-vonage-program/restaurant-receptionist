package com.restaurantreceptionist.reservations;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.OptBoolean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public record ManualReservationRequest(
	String customerFirstName,
	String customerLastName,
	String phoneNumber,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd", lenient = OptBoolean.FALSE)
	LocalDate reservationDate,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", lenient = OptBoolean.FALSE)
	LocalTime arrivalTime,
	Integer partySize) {

	ConfirmedReservationCommand toCommand() {
		Objects.requireNonNull(partySize, "partySize must be checked before mapping to the command");
		return new ConfirmedReservationCommand(
			customerFirstName,
			customerLastName,
			phoneNumber,
			reservationDate,
			arrivalTime,
			partySize);
	}

	@Override
	public String toString() {
		return "ManualReservationRequest[reservationDate=" + reservationDate
			+ ", arrivalTime=" + arrivalTime
			+ ", partySize=" + partySize
			+ ", personalData=<redacted>]";
	}
}
