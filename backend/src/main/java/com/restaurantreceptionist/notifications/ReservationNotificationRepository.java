package com.restaurantreceptionist.notifications;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationNotificationRepository extends JpaRepository<ReservationNotification, UUID> {

	Optional<ReservationNotification> findByReservationId(UUID reservationId);

	Optional<ReservationNotification> findByProviderMessageId(String providerMessageId);
}
