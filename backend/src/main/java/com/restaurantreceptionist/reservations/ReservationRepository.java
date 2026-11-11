package com.restaurantreceptionist.reservations;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

	List<Reservation> findAllByReservationDateAndDiningSessionOrderByArrivalTimeAsc(
			LocalDate reservationDate, DiningSession diningSession);

	@Query("""
		SELECT COALESCE(SUM(r.partySize), 0L)
		FROM Reservation r
		WHERE r.reservationDate = :reservationDate
			AND r.diningSession = :diningSession
			AND r.status = com.restaurantreceptionist.reservations.ReservationStatus.CONFIRMED
		""")
	long sumConfirmedPartySize(
			@Param("reservationDate") LocalDate reservationDate,
			@Param("diningSession") DiningSession diningSession);
}
