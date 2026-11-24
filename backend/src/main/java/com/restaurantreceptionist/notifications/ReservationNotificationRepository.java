package com.restaurantreceptionist.notifications;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;

public interface ReservationNotificationRepository extends JpaRepository<ReservationNotification, UUID> {

	Optional<ReservationNotification> findByReservationId(UUID reservationId);

	Optional<ReservationNotification> findByProviderMessageId(String providerMessageId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Transactional(propagation = Propagation.MANDATORY)
	Optional<ReservationNotification> findForUpdateById(UUID id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Transactional(propagation = Propagation.MANDATORY)
	Optional<ReservationNotification> findForUpdateByProviderMessageId(String providerMessageId);
}
