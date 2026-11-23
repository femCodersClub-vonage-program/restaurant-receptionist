package com.restaurantreceptionist;

import com.restaurantreceptionist.notifications.NotificationFailureReason;
import com.restaurantreceptionist.notifications.NotificationStatus;
import com.restaurantreceptionist.notifications.ReservationNotification;
import com.restaurantreceptionist.notifications.ReservationNotificationRepository;
import com.restaurantreceptionist.reservations.DiningSession;
import com.restaurantreceptionist.reservations.Reservation;
import com.restaurantreceptionist.reservations.ReservationRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class NotificationPersistenceIntegrationTests {

	private static final String MESSAGE_ID = "message-1";

	@Autowired
	private Flyway flyway;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private ReservationNotificationRepository notificationRepository;

	@Test
	void appliesReservationNotificationsMigration() {
		assertThat(flyway.info().applied())
			.extracting(migration -> migration.getVersion().getVersion())
			.contains("2");
		assertThat(jdbcTemplate.queryForObject(
			"SELECT to_regclass('public.reservation_notifications') IS NOT NULL", Boolean.class)).isTrue();
	}

	@ParameterizedTest
	@EnumSource(NotificationStatus.class)
	void persistsEveryStatusWithoutChangingReservation(NotificationStatus status) {
		var reservation = savedReservation();

		var notification = notificationRepository.saveAndFlush(notificationIn(status, reservation.getId()));

		assertThat(jdbcTemplate.queryForObject(
			"SELECT status FROM reservation_notifications WHERE id = ?", String.class, notification.getId()))
			.isEqualTo(status.name());
		assertThat(jdbcTemplate.queryForObject(
			"SELECT status FROM reservations WHERE id = ?", String.class, reservation.getId()))
			.isEqualTo("CONFIRMED");
		assertThat(notification.getCreatedAt()).isNotNull();
	}

	@Test
	void findsNotificationByReservationAndByProviderMessageId() {
		var reservation = savedReservation();
		var notification = notificationRepository.saveAndFlush(notificationIn(NotificationStatus.ACCEPTED, reservation.getId()));

		assertThat(notificationRepository.findByReservationId(reservation.getId()))
			.hasValueSatisfying(found -> assertThat(found.getId()).isEqualTo(notification.getId()));
		assertThat(notificationRepository.findByProviderMessageId(MESSAGE_ID))
			.hasValueSatisfying(found -> assertThat(found.getId()).isEqualTo(notification.getId()));
		assertThat(notificationRepository.findByProviderMessageId("unknown-message")).isEmpty();
	}

	@Test
	void rejectsSecondNotificationForSameReservation() {
		var reservation = savedReservation();
		notificationRepository.saveAndFlush(ReservationNotification.pending(reservation.getId()));

		assertThatThrownBy(() -> notificationRepository.saveAndFlush(ReservationNotification.pending(reservation.getId())))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsDuplicateProviderMessageId() {
		notificationRepository.saveAndFlush(notificationIn(NotificationStatus.ACCEPTED, savedReservation().getId()));
		var duplicate = notificationIn(NotificationStatus.ACCEPTED, savedReservation().getId());

		assertThatThrownBy(() -> notificationRepository.saveAndFlush(duplicate))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsNotificationForUnknownReservation() {
		var orphan = ReservationNotification.pending(UUID.fromString("00000000-0000-0000-0000-000000000099"));

		assertThatThrownBy(() -> notificationRepository.saveAndFlush(orphan))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@ParameterizedTest
	@CsvSource({
		"READ, , ",
		"ACCEPTED, , ",
		"PENDING, message-1, ",
		"REQUEST_FAILED, , ",
		"REQUEST_FAILED, , PROVIDER_OUTCOME_UNKNOWN",
		"REQUEST_OUTCOME_UNKNOWN, , ",
		"REQUEST_OUTCOME_UNKNOWN, , PROVIDER_UNAVAILABLE",
		"DELIVERED, message-1, PROVIDER_UNAVAILABLE"
	})
	void rejectsInconsistentNotificationRow(String status, String providerMessageId, String failureReason) {
		var reservationId = savedReservation().getId();

		assertThatThrownBy(() -> jdbcTemplate.update("""
                                INSERT INTO reservation_notifications (reservation_id, status, provider_message_id, failure_reason)
                                VALUES (?, ?, ?, ?)
                                """, reservationId, status, providerMessageId, failureReason))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Reservation savedReservation() {
		return reservationRepository.saveAndFlush(new Reservation(
			"Fictional",
			"Guest",
			"fictional-test-contact",
			LocalDate.of(2027, 5, 14),
			LocalTime.of(13, 30),
			DiningSession.LUNCH,
			(short) 4));
	}

	private static ReservationNotification notificationIn(NotificationStatus status, UUID reservationId) {
		return switch (status) {
			case NOT_REQUESTED_CANNOT_RECEIVE -> ReservationNotification.notRequestedCannotReceive(reservationId);
			case NOT_REQUESTED_CAPABILITY_UNKNOWN -> ReservationNotification.notRequestedCapabilityUnknown(reservationId);
			case PENDING -> ReservationNotification.pending(reservationId);
			case REQUEST_FAILED -> {
				var notification = ReservationNotification.pending(reservationId);
				notification.markRequestFailed(NotificationFailureReason.PROVIDER_UNAVAILABLE);
				yield notification;
			}
			case REQUEST_OUTCOME_UNKNOWN -> {
				var notification = ReservationNotification.pending(reservationId);
				notification.markOutcomeUnknown();
				yield notification;
			}
			case ACCEPTED -> {
				var notification = ReservationNotification.pending(reservationId);
				notification.markAccepted(MESSAGE_ID);
				yield notification;
			}
			case SUBMITTED, DELIVERED, REJECTED, UNDELIVERABLE -> {
				var notification = notificationIn(NotificationStatus.ACCEPTED, reservationId);
				notification.applyProviderStatus(status, MESSAGE_ID);
				yield notification;
			}
		};
	}
}
