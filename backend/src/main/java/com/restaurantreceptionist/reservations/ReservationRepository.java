package com.restaurantreceptionist.reservations;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

	List<Reservation> findAllByReservationDateAndServiceOrderByArrivalTimeAsc(
			LocalDate reservationDate, ReservationService service);
}
