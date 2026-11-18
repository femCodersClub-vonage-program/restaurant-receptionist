package com.restaurantreceptionist.reservations;

import com.restaurantreceptionist.availability.ArrivalWindows;
import com.restaurantreceptionist.availability.AvailabilityService;
import com.restaurantreceptionist.availability.PartyFitResult;
import com.restaurantreceptionist.availability.ServiceCapacity;
import com.restaurantreceptionist.reservations.ReservationCreationResult.Created;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InsufficientCapacity;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InvalidData;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InvalidData.Reason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class ReservationCreationService {

	private final AvailabilityService availabilityService;

	private final ReservationRepository reservationRepository;

	private final Clock clock;

	public ReservationCreationService(
		AvailabilityService availabilityService,
		ReservationRepository reservationRepository,
		Clock clock) {
		this.availabilityService = availabilityService;
		this.reservationRepository = reservationRepository;
		this.clock = clock;
	}

	@Transactional
	public ReservationCreationResult create(ConfirmedReservationCommand command) {
		Objects.requireNonNull(command, "command must not be null");

		Optional<InvalidData> invalidData = validate(command);
		if (invalidData.isPresent()) {
			return invalidData.get();
		}
		String normalizedPhoneNumber = normalizeSpanishPhoneNumber(command.phoneNumber());
		if (normalizedPhoneNumber == null) {
			return new InvalidData(ReservationField.PHONE_NUMBER, Reason.INVALID_FORMAT);
		}

		LocalDate reservationDate = command.reservationDate();
		DiningSession diningSession = ArrivalWindows.classify(command.arrivalTime()).orElseThrow();

		reservationRepository.lockCapacity(reservationDate, diningSession);
		PartyFitResult partyFit = availabilityService.checkPartyFit(
			reservationDate, command.arrivalTime(), command.partySize());

		return switch (partyFit) {
			case PartyFitResult.Fits fits -> persist(command, normalizedPhoneNumber, diningSession);
			case PartyFitResult.InsufficientCapacity insufficient ->
				new InsufficientCapacity(insufficient.capacity(), insufficient.requestedPartySize());
			case PartyFitResult.InvalidArrivalTime invalidTime ->
				new InvalidData(ReservationField.ARRIVAL_TIME, Reason.OUTSIDE_ARRIVAL_WINDOW);
			case PartyFitResult.InvalidPartySize invalidSize ->
				new InvalidData(ReservationField.PARTY_SIZE, Reason.OUT_OF_RANGE);
		};
	}

	private Optional<InvalidData> validate(ConfirmedReservationCommand command) {
		if (isBlank(command.firstName())) {
			return invalid(ReservationField.FIRST_NAME, Reason.MISSING);
		}
		if (isBlank(command.lastName())) {
			return invalid(ReservationField.LAST_NAME, Reason.MISSING);
		}
		if (isBlank(command.phoneNumber())) {
			return invalid(ReservationField.PHONE_NUMBER, Reason.MISSING);
		}
		if (command.reservationDate() == null) {
			return invalid(ReservationField.RESERVATION_DATE, Reason.MISSING);
		}
		if (command.arrivalTime() == null) {
			return invalid(ReservationField.ARRIVAL_TIME, Reason.MISSING);
		}

		LocalDate today = LocalDate.now(clock);
		if (command.reservationDate().isBefore(today)) {
			return invalid(ReservationField.RESERVATION_DATE, Reason.PAST_DATE);
		}

		if (ArrivalWindows.classify(command.arrivalTime()).isEmpty()) {
			return invalid(ReservationField.ARRIVAL_TIME, Reason.OUTSIDE_ARRIVAL_WINDOW);
		}

		if (command.reservationDate().equals(today) && !command.arrivalTime().isAfter(LocalTime.now(clock))) {
			return invalid(ReservationField.ARRIVAL_TIME, Reason.TIME_NOT_IN_FUTURE);
		}

		if (command.partySize() < ServiceCapacity.MIN_PARTY_SIZE || command.partySize() > ServiceCapacity.GUESTS_PER_SERVICE) {
			return invalid(ReservationField.PARTY_SIZE, Reason.OUT_OF_RANGE);
		}
		return Optional.empty();
	}

	private Created persist(
		ConfirmedReservationCommand command,
		String normalizedPhoneNumber,
		DiningSession diningSession) {
		Reservation reservation = reservationRepository.saveAndFlush(new Reservation(
			command.firstName().strip(),
			command.lastName().strip(),
			normalizedPhoneNumber,
			command.reservationDate(),
			command.arrivalTime(),
			diningSession,
			(short) command.partySize()));
		return new Created(reservation.getId(), reservation.getDiningSession());
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private static String normalizeSpanishPhoneNumber(String phoneNumber) {
		String compact = phoneNumber.strip().replace(" ", "").replace("-", "");
		String nationalNumber = compact.startsWith("+34") ? compact.substring(3) : compact;
		if (!nationalNumber.matches("[6-9]\\d{8}")) {
			return null;
		}
		return "+34" + nationalNumber;
	}

	private static Optional<InvalidData> invalid(ReservationField field, Reason reason) {
		return Optional.of(new InvalidData(field, reason));
	}
}
