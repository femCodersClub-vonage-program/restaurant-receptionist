package com.restaurantreceptionist.reservations;

import com.restaurantreceptionist.availability.AvailabilityService;
import com.restaurantreceptionist.availability.CapacitySummary;
import com.restaurantreceptionist.availability.PartyFitResult;
import com.restaurantreceptionist.config.ClockConfiguration;
import com.restaurantreceptionist.reservations.ReservationCreationResult.Created;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InsufficientCapacity;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InvalidData;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InvalidData.Reason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationCreationServiceTests {

	private static final LocalDate TODAY = LocalDate.of(2027, 5, 14);

	private static final LocalDate TOMORROW = TODAY.plusDays(1);

	private static final LocalTime NOW = LocalTime.of(14, 0);

	private static final LocalTime LUNCH_TIME = LocalTime.of(13, 30);

	private static final Clock CLOCK = Clock.fixed(
		TODAY.atTime(NOW).atZone(ClockConfiguration.RESTAURANT_ZONE).toInstant(),
		ClockConfiguration.RESTAURANT_ZONE);

	@Mock
	private AvailabilityService availabilityService;

	@Mock
	private ReservationRepository reservationRepository;

	@Captor
	private ArgumentCaptor<Reservation> reservationCaptor;

	private ReservationCreationService reservationCreationService;

	@BeforeEach
	void setUp() {
		reservationCreationService = new ReservationCreationService(
			availabilityService, reservationRepository, CLOCK
		);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = "   ")
	void rejectsMissingFirstName(String firstName) {
		var command = new ConfirmedReservationCommand(
			firstName, "Guest", "fictional-test-contact", TOMORROW, LUNCH_TIME, 2);

		assertInvalid(command, ReservationField.FIRST_NAME, Reason.MISSING);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = "   ")
	void rejectsMissingLastName(String lastName) {
		var command = new ConfirmedReservationCommand(
			"Fictional", lastName, "fictional-test-contact", TOMORROW, LUNCH_TIME, 2);

		assertInvalid(command, ReservationField.LAST_NAME, Reason.MISSING);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = "   ")
	void rejectsMissingPhoneNumber(String phoneNumber) {
		var command = new ConfirmedReservationCommand(
			"Fictional", "Guest", phoneNumber, TOMORROW, LUNCH_TIME, 2);

		assertInvalid(command, ReservationField.PHONE_NUMBER, Reason.MISSING);
	}

	@Test
	void rejectsMissingReservationDate() {
		assertInvalid(command(null, LUNCH_TIME, 2), ReservationField.RESERVATION_DATE, Reason.MISSING);
	}

	@Test
	void rejectsMissingArrivalTime() {
		assertInvalid(command(TOMORROW, null, 2), ReservationField.ARRIVAL_TIME, Reason.MISSING);
	}

	@Test
	void rejectsPastDate() {
		assertInvalid(command(TODAY.minusDays(1), LUNCH_TIME, 2), ReservationField.RESERVATION_DATE, Reason.PAST_DATE);
	}

	@ParameterizedTest
	@ValueSource(strings = {"13:30", "14:00"})
	void rejectsSameDayArrivalTimeNotInFuture(LocalTime arrivalTime) {
		assertInvalid(command(TODAY, arrivalTime, 2), ReservationField.ARRIVAL_TIME, Reason.TIME_NOT_IN_FUTURE);
	}

	@ParameterizedTest
	@ValueSource(strings = {"12:59", "16:01", "19:59", "23:31"})
	void rejectsArrivalTimeOutsideWindows(LocalTime arrivalTime) {
		assertInvalid(command(TOMORROW, arrivalTime, 2), ReservationField.ARRIVAL_TIME, Reason.OUTSIDE_ARRIVAL_WINDOW);
	}

	@Test
	void reportsArrivalWindowBeforeSameDayTime() {
		assertInvalid(command(TODAY, LocalTime.of(10, 0), 2), ReservationField.ARRIVAL_TIME, Reason.OUTSIDE_ARRIVAL_WINDOW);
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, 0, 31})
	void rejectsPartySizeOutOfRange(int partySize) {
		assertInvalid(command(TOMORROW, LUNCH_TIME, partySize), ReservationField.PARTY_SIZE, Reason.OUT_OF_RANGE);
	}

	@Test
	void createsReservationAfterLockingAndRevalidatingCapacity() {
		var command = new ConfirmedReservationCommand(
			" Fictional ", " Guest ", " fictional-test-contact ", TOMORROW, LUNCH_TIME, 4);

		when(availabilityService.checkPartyFit(TOMORROW, LUNCH_TIME, 4))
			.thenReturn(new PartyFitResult.Fits( new CapacitySummary(DiningSession.LUNCH, 30, 26, 4)));

		when(reservationRepository.saveAndFlush(any(Reservation.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		var result = reservationCreationService.create(command);

		assertThat(result).isInstanceOfSatisfying(Created.class, created -> assertThat(created.diningSession()).isEqualTo(DiningSession.LUNCH));

		InOrder inOrder = inOrder(reservationRepository, availabilityService);
		inOrder.verify(reservationRepository).lockCapacity(TOMORROW, DiningSession.LUNCH);
		inOrder.verify(availabilityService).checkPartyFit(TOMORROW, LUNCH_TIME, 4);
		inOrder.verify(reservationRepository).saveAndFlush(reservationCaptor.capture());

		assertThat(reservationCaptor.getValue())
			.extracting(
				Reservation::getCustomerFirstName,
				Reservation::getCustomerLastName,
				Reservation::getPhoneNumber,
				Reservation::getReservationDate,
				Reservation::getArrivalTime,
				Reservation::getDiningSession,
				Reservation::getPartySize,
				Reservation::getStatus)
			.containsExactly(
				"Fictional", "Guest", "fictional-test-contact",
				TOMORROW, LUNCH_TIME, DiningSession.LUNCH, (short) 4, ReservationStatus.CONFIRMED);
	}

	@Test
	void acceptsSameDayArrivalTimeAfterNow() {
		var arrivalTime = NOW.plusMinutes(30);
		stubSuccessfulCreation(TODAY, arrivalTime, 2, DiningSession.LUNCH);

		assertThat(reservationCreationService.create(command(TODAY, arrivalTime, 2)))
			.isInstanceOf(Created.class);
	}

	@Test
	void derivesDinnerSessionFromArrivalTime() {
		var dinnerTime = LocalTime.of(21, 0);
		stubSuccessfulCreation(TOMORROW, dinnerTime, 2, DiningSession.DINNER);

		var result = reservationCreationService.create(command(TOMORROW, dinnerTime, 2));

		assertThat(result).isInstanceOfSatisfying(Created.class,
			created -> assertThat(created.diningSession()).isEqualTo(DiningSession.DINNER));
		verify(reservationRepository).lockCapacity(TOMORROW, DiningSession.DINNER);
	}

	@Test
	void reportsInsufficientCapacityWithoutPersisting() {
		var capacity = new CapacitySummary(DiningSession.LUNCH, 30, 28, 2);
		when(availabilityService.checkPartyFit(TOMORROW, LUNCH_TIME, 4))
			.thenReturn(new PartyFitResult.InsufficientCapacity(capacity, 4));

		var result = reservationCreationService.create(command(TOMORROW, LUNCH_TIME, 4));

		assertThat(result).isEqualTo(new InsufficientCapacity(capacity, 4));
		verify(reservationRepository).lockCapacity(TOMORROW, DiningSession.LUNCH);
		verify(reservationRepository, never()).saveAndFlush(any());
	}

	@Test
	void mapsUnexpectedArrivalTimeRejectionFromRevalidation() {
		when(availabilityService.checkPartyFit(TOMORROW, LUNCH_TIME, 2))
			.thenReturn(new PartyFitResult.InvalidArrivalTime(LUNCH_TIME));

		assertThat(reservationCreationService.create(command(TOMORROW, LUNCH_TIME, 2)))
			.isEqualTo(new InvalidData(ReservationField.ARRIVAL_TIME, Reason.OUTSIDE_ARRIVAL_WINDOW));
		verify(reservationRepository, never()).saveAndFlush(any());
	}

	@Test
	void mapsUnexpectedPartySizeRejectionFromRevalidation() {
		when(availabilityService.checkPartyFit(TOMORROW, LUNCH_TIME, 2))
			.thenReturn(new PartyFitResult.InvalidPartySize(2));

		assertThat(reservationCreationService.create(command(TOMORROW, LUNCH_TIME, 2)))
			.isEqualTo(new InvalidData(ReservationField.PARTY_SIZE, Reason.OUT_OF_RANGE));
		verify(reservationRepository, never()).saveAndFlush(any());
	}

	private void assertInvalid(ConfirmedReservationCommand command, ReservationField field, Reason reason) {
		assertThat(reservationCreationService.create(command)).isEqualTo(new InvalidData(field, reason));
		verifyNoInteractions(reservationRepository, availabilityService);
	}

	private static ConfirmedReservationCommand command(LocalDate date, LocalTime time, int partySize) {
		return new ConfirmedReservationCommand(
			"Fictional", "Guest", "fictional-test-contact", date, time, partySize);
	}
	private void stubSuccessfulCreation(
		LocalDate date, LocalTime time, int partySize, DiningSession diningSession) {
		when(availabilityService.checkPartyFit(date, time, partySize))
			.thenReturn(new PartyFitResult.Fits(new CapacitySummary(diningSession, 30, 0, 30)));
		when(reservationRepository.saveAndFlush(any(Reservation.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
	}
}
