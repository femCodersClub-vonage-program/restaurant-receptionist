package com.restaurantreceptionist;

import com.restaurantreceptionist.availability.AvailabilityService;
import com.restaurantreceptionist.availability.CapacitySummary;
import com.restaurantreceptionist.availability.PartyFitResult;
import com.restaurantreceptionist.reservations.DiningSession;
import com.restaurantreceptionist.reservations.Reservation;
import com.restaurantreceptionist.reservations.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class AvailabilityServiceIntegrationTests {

	private static final LocalDate DATE = LocalDate.of(2027, 5, 14);

	@Autowired
	private AvailabilityService availabilityService;

	@Autowired
	private ReservationRepository reservationRepository;

	@Test
	void reportsFullCapacityWhenNoReservationsExist() {
		assertThat(availabilityService.calculateCapacity(DATE, DiningSession.LUNCH))
			.isEqualTo(new CapacitySummary(DiningSession.LUNCH, 30, 0, 30));
	}

	@Test
	void calculatesCapacityFromPersistedReservationsForSameDateAndService() {
		persistMixedReservations();

		assertThat(availabilityService.calculateCapacity(DATE, DiningSession.LUNCH))
			.isEqualTo(new CapacitySummary(DiningSession.LUNCH, 30, 7, 23));
	}

	@Test
	void checksPartyFitAgainstPersistedReservations() {
		persistMixedReservations();
		var expectedCapacity = new CapacitySummary(DiningSession.LUNCH, 30, 7, 23);

		assertThat(availabilityService.checkPartyFit(DATE, LocalTime.of(14, 0), 23))
			.isEqualTo(new PartyFitResult.Fits(expectedCapacity));
		assertThat(availabilityService.checkPartyFit(DATE, LocalTime.of(14, 0), 24))
			.isEqualTo(new PartyFitResult.InsufficientCapacity(expectedCapacity, 24));
	}

	private void persistMixedReservations() {
		reservationRepository.saveAllAndFlush(List.of(
			reservation(DATE, LocalTime.of(13, 0), DiningSession.LUNCH, (short) 2),
			reservation(DATE, LocalTime.of(15, 30), DiningSession.LUNCH, (short) 5),
			reservation(DATE, LocalTime.of(21, 0), DiningSession.DINNER, (short) 8),
			reservation(DATE.plusDays(1), LocalTime.of(13, 0), DiningSession.LUNCH, (short) 6)));
	}

	private Reservation reservation(
		LocalDate date, LocalTime time, DiningSession diningSession, short partySize) {
		return new Reservation(
			"Fictional",
			"Guest",
			"fictional-test-contact",
			date,
			time,
			diningSession,
			partySize
		);
	}
}
