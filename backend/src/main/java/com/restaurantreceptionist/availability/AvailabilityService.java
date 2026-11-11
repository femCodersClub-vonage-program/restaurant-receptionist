package com.restaurantreceptionist.availability;

import com.restaurantreceptionist.reservations.DiningSession;
import com.restaurantreceptionist.reservations.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {

	private final ReservationRepository reservationRepository;

	public AvailabilityService(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	public Optional<DiningSession> deriveDiningSession(LocalTime arrivalTime) {
		return ArrivalWindows.classify(arrivalTime);
	}

	public CapacitySummary calculateCapacity(LocalDate reservationDate, DiningSession diningSession) {
		Objects.requireNonNull(reservationDate, "reservationDate must not be null");
		Objects.requireNonNull(diningSession, "diningSession must not be null");
		long occupiedGuests = reservationRepository.sumConfirmedPartySize(reservationDate, diningSession);
		return CapacitySummary.fromOccupied(diningSession, occupiedGuests);
	}

	public PartyFitResult checkPartyFit(LocalDate reservationDate, LocalTime arrivalTime, int partySize) {
		Objects.requireNonNull(reservationDate, "reservationDate must not be null");

		Optional<DiningSession> diningSession = ArrivalWindows.classify(arrivalTime);
		if (diningSession.isEmpty()) {
			return new PartyFitResult.InvalidArrivalTime(arrivalTime);
		}

		if (partySize < ServiceCapacity.MIN_PARTY_SIZE || partySize > ServiceCapacity.GUESTS_PER_SERVICE) {
			return new PartyFitResult.InvalidPartySize(partySize);
		}

		CapacitySummary capacity = calculateCapacity(reservationDate, diningSession.get());
		if (partySize > capacity.availableCapacity()) {
			return new PartyFitResult.InsufficientCapacity(capacity, partySize);
		}
		return new PartyFitResult.Fits(capacity);
	}
}
