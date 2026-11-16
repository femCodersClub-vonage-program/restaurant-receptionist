package com.restaurantreceptionist.reservations;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ConfirmedReservationCommandTests {

	@Test
	void toStringDoesNotExposePersonalData() {
		var command = new ConfirmedReservationCommand(
			"Fictional", "Guest", "fictional-test-contact", LocalDate.of(2027, 5, 15), LocalTime.of(13, 30), 4);

		assertThat(command.toString())
			.doesNotContain("Fictional", "Guest", "fictional-test-contact")
			.contains("2027-05-15", "13:30", "partySize=4", "personalData=<redacted>");
	}
}
