package com.restaurantreceptionist.availability;

import com.restaurantreceptionist.reservations.DiningSession;
import com.restaurantreceptionist.reservations.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTests {

	private static final LocalDate DATE = LocalDate.of(2027, 5, 14);

	private static final LocalTime LUNCH_TIME = LocalTime.of(13, 30);

	private static final LocalTime DINNER_TIME = LocalTime.of(21, 0);

	@Mock
	private ReservationRepository reservationRepository;

	@InjectMocks
	private AvailabilityService availabilityService;

	@Test
	void calculatesZeroOccupancy() {
		when(reservationRepository.sumConfirmedPartySize(DATE, DiningSession.LUNCH)).thenReturn(0L);

		assertThat(availabilityService.calculateCapacity(DATE, DiningSession.LUNCH))
			.isEqualTo(new CapacitySummary(DiningSession.LUNCH, 30, 0, 30));
	}

	@Test
	void calculatesPartialOccupancy() {
		when(reservationRepository.sumConfirmedPartySize(DATE, DiningSession.LUNCH)).thenReturn(26L);

		assertThat(availabilityService.calculateCapacity(DATE, DiningSession.LUNCH))
			.isEqualTo(new CapacitySummary(DiningSession.LUNCH, 30, 26, 4));
	}

	@Test
	void calculatesFullOccupancy() {
		when(reservationRepository.sumConfirmedPartySize(DATE, DiningSession.LUNCH)).thenReturn(30L);

		assertThat(availabilityService.calculateCapacity(DATE, DiningSession.LUNCH))
			.isEqualTo(new CapacitySummary(DiningSession.LUNCH, 30, 30, 0));
	}

	@Test
	void fitsPartyInEmptyService() {
		when(reservationRepository.sumConfirmedPartySize(DATE, DiningSession.LUNCH)).thenReturn(0L);

		assertThat(availabilityService.checkPartyFit(DATE, LUNCH_TIME, 4))
			.isEqualTo(new PartyFitResult.Fits(new CapacitySummary(DiningSession.LUNCH, 30, 0, 30)));
	}

	@Test
	void fitsPartyThatExactlyFillsRemainingCapacity() {
		when(reservationRepository.sumConfirmedPartySize(DATE, DiningSession.LUNCH)).thenReturn(26L);

		assertThat(availabilityService.checkPartyFit(DATE, LUNCH_TIME, 4))
			.isEqualTo(new PartyFitResult.Fits(new CapacitySummary(DiningSession.LUNCH, 30, 26, 4)));
	}

	@Test
	void reportsInsufficientCapacityWhenPartyExceedsRemainingCapacity() {
		when(reservationRepository.sumConfirmedPartySize(DATE, DiningSession.LUNCH)).thenReturn(26L);

		assertThat(availabilityService.checkPartyFit(DATE, LUNCH_TIME, 5))
			.isEqualTo(new PartyFitResult.InsufficientCapacity(
				new CapacitySummary(DiningSession.LUNCH, 30, 26, 4), 5));
	}

	@Test
	void reportsInsufficientCapacityWhenServiceIsFull() {
		when(reservationRepository.sumConfirmedPartySize(DATE, DiningSession.LUNCH)).thenReturn(30L);

		assertThat(availabilityService.checkPartyFit(DATE, LUNCH_TIME, 1))
			.isEqualTo(new PartyFitResult.InsufficientCapacity(
				new CapacitySummary(DiningSession.LUNCH, 30, 30, 0), 1));
	}

	@Test
	void usesDinnerCapacityForDinnerArrivalTime() {
		when(reservationRepository.sumConfirmedPartySize(DATE, DiningSession.DINNER)).thenReturn(10L);

		assertThat(availabilityService.checkPartyFit(DATE, DINNER_TIME, 2))
			.isEqualTo(new PartyFitResult.Fits(new CapacitySummary(DiningSession.DINNER, 30, 10, 20)));
		verify(reservationRepository).sumConfirmedPartySize(DATE, DiningSession.DINNER);
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, 0, 31})
	void rejectsPartySizeOutsideAllowedRange(int partySize) {
		assertThat(availabilityService.checkPartyFit(DATE, LUNCH_TIME, partySize))
			.isEqualTo(new PartyFitResult.InvalidPartySize(partySize));
		verifyNoInteractions(reservationRepository);
	}

	@Test
	void rejectsArrivalTimeOutsideWindows() {
		LocalTime invalidTime = LocalTime.of(17, 30);

		assertThat(availabilityService.checkPartyFit(DATE, invalidTime, 4))
			.isEqualTo(new PartyFitResult.InvalidArrivalTime(invalidTime));
		verifyNoInteractions(reservationRepository);
	}
}
