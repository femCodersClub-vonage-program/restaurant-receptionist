package com.restaurantreceptionist.reservations;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public record ManualReservationRequest(
	String customerFirstName,
	String customerLastName,
	String phoneNumber,
	@JsonDeserialize(using = StrictLocalDateDeserializer.class)
	LocalDate reservationDate,
	@Schema(type = "string", pattern = "^([01]\\d|2[0-3]):[0-5]\\d$", example = "21:00")
	@JsonDeserialize(using = StrictLocalTimeDeserializer.class)
	LocalTime arrivalTime,
	@JsonDeserialize(using = StrictIntegerDeserializer.class)
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
