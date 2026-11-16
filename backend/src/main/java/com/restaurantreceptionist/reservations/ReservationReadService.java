package com.restaurantreceptionist.reservations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantreceptionist.availability.AvailabilityService;
import com.restaurantreceptionist.availability.CapacitySummary;

@Service
@Transactional(readOnly = true)
public class ReservationReadService {

	private static final DateTimeFormatter ARRIVAL_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private final ReservationRepository reservationRepository;
	private final AvailabilityService availabilityService;

	public ReservationReadService(
			ReservationRepository reservationRepository,
			AvailabilityService availabilityService) {
		this.reservationRepository = reservationRepository;
		this.availabilityService = availabilityService;
	}

	public ReservationReadResponse findByDate(LocalDate date) {
		return new ReservationReadResponse(
				date,
				new ReservationReadResponse.Sessions(
						readSession(date, DiningSession.LUNCH),
						readSession(date, DiningSession.DINNER)));
	}

	private ReservationReadResponse.SessionReservations readSession(
			LocalDate date, DiningSession diningSession) {
		var reservations = reservationRepository
				.findAllByReservationDateAndDiningSessionOrderByArrivalTimeAsc(date, diningSession)
				.stream()
				.map(ReservationReadService::toItem)
				.toList();
		CapacitySummary capacity = availabilityService.calculateCapacity(date, diningSession);

		return new ReservationReadResponse.SessionReservations(
				reservations,
				new ReservationReadResponse.Capacity(
						capacity.totalCapacity(),
						capacity.occupiedCapacity(),
						capacity.availableCapacity()));
	}

	private static ReservationReadResponse.ReservationItem toItem(Reservation reservation) {
		return new ReservationReadResponse.ReservationItem(
				reservation.getId(),
				reservation.getCustomerFirstName(),
				reservation.getCustomerLastName(),
				reservation.getPhoneNumber(),
				reservation.getReservationDate(),
				reservation.getArrivalTime().format(ARRIVAL_TIME_FORMAT),
				reservation.getPartySize(),
				reservation.getStatus());
	}
}
