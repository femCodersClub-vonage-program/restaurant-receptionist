package com.restaurantreceptionist.reservations;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional(propagation = Propagation.MANDATORY)
	default void lockCapacity(LocalDate reservationDate, DiningSession diningSession) {
		int dateKey = Math.toIntExact(reservationDate.toEpochDay());
		int sessionKey = switch (diningSession) {
			case LUNCH -> 1;
			case DINNER -> 2;
		};
		acquireCapacityLock(dateKey, sessionKey);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Query(value = "SELECT 1 FROM pg_advisory_xact_lock(:dateKey, :sessionKey)", nativeQuery = true)
	int acquireCapacityLock(@Param("dateKey") int dateKey, @Param("sessionKey") int sessionKey);
}
