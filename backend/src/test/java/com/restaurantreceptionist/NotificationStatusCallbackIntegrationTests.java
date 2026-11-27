package com.restaurantreceptionist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

import com.restaurantreceptionist.notifications.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.restaurantreceptionist.reservations.DiningSession;
import com.restaurantreceptionist.reservations.Reservation;
import com.restaurantreceptionist.reservations.ReservationRepository;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class NotificationStatusCallbackIntegrationTests {

	private static final String MESSAGE_ID = "fictional-message-1";

	private static final String OTHER_MESSAGE_ID = "fictional-message-2";

	@Autowired
	private NotificationStatusCallbackService callbackService;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private ReservationNotificationRepository notificationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private final List<UUID> reservationIds = new ArrayList<>();

	@AfterEach
	void tearDown() {
		for (UUID reservationId : reservationIds) {
			jdbcTemplate.update("DELETE FROM reservation_notifications WHERE reservation_id = ?", reservationId);
			jdbcTemplate.update("DELETE FROM reservations WHERE id = ?", reservationId);
		}
	}

	@Test
	void appliesStatusMatchedByMessageUuid() {
		var notificationId = committedNotification(notification -> notification.markAccepted(MESSAGE_ID));

		var outcome = apply(MESSAGE_ID, NotificationStatus.DELIVERED, null);

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.APPLIED);
		assertStored(notificationId, NotificationStatus.DELIVERED, MESSAGE_ID);
	}

	@Test
	void resolvesPendingNotificationThroughClientRef() {
		var notificationId = committedNotification(notification -> { });

		var outcome = apply(MESSAGE_ID, NotificationStatus.SUBMITTED, notificationId.toString());

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.APPLIED);
		assertStored(notificationId, NotificationStatus.SUBMITTED, MESSAGE_ID);
	}

	@Test
	void resolvesUnknownRequestOutcomeThroughClientRefAndClearsFailureReason() {
		var notificationId = committedNotification(ReservationNotification::markOutcomeUnknown);

		var outcome = apply(MESSAGE_ID, NotificationStatus.DELIVERED, notificationId.toString());

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.APPLIED);
		assertStored(notificationId, NotificationStatus.DELIVERED, MESSAGE_ID);
	}

	@Test
	void ignoresClientRefFallbackWhenStoredMessageUuidDiffers() {
		var notificationId = committedNotification(notification -> notification.markAccepted(MESSAGE_ID));

		var outcome = apply(OTHER_MESSAGE_ID, NotificationStatus.DELIVERED, notificationId.toString());

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.IGNORED);
		assertStored(notificationId, NotificationStatus.ACCEPTED, MESSAGE_ID);
	}

	@Test
	void leavesNotificationsUnchangedWhenCorrelationIsAmbiguous() {
		var matchedId = committedNotification(notification -> notification.markAccepted(MESSAGE_ID));
		var otherId = committedNotification(notification -> { });

		var outcome = apply(MESSAGE_ID, NotificationStatus.DELIVERED, otherId.toString());

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.AMBIGUOUS_CORRELATION);
		assertStored(matchedId, NotificationStatus.ACCEPTED, MESSAGE_ID);
		assertStored(otherId, NotificationStatus.PENDING, null);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "not-a-uuid", "1-1-1-1-1", "00000000-0000-0000-0000-00000000ffff" })
	 void leavesNotificationsUnchangedWhenCorrelationIsUnknown(String clientRef) {
		var notificationId = committedNotification(notification -> { });

		var outcome = apply(MESSAGE_ID, NotificationStatus.DELIVERED, clientRef);

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.UNKNOWN_CORRELATION);
		assertStored(notificationId, NotificationStatus.PENDING, null);
	}

	@Test
	void leavesNotificationUnchangedWhenClientRefIsAnotherValidButMissingNotification() {
		var matchedId = committedNotification(notification -> notification.markAccepted(MESSAGE_ID));
		var missingNotificationId = "00000000-0000-0000-0000-00000000ffff";

		var outcome = apply(MESSAGE_ID, NotificationStatus.DELIVERED, missingNotificationId);

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.AMBIGUOUS_CORRELATION);
		assertStored(matchedId, NotificationStatus.ACCEPTED, MESSAGE_ID);
	}

	@Test
	void repeatedCallbackIsIgnoredWithoutWriting() {
		var notificationId = committedNotification(notification -> notification.markAccepted(MESSAGE_ID));
		assertThat(apply(MESSAGE_ID, NotificationStatus.SUBMITTED, null)).isEqualTo(StatusCallbackOutcome.APPLIED);
		var updatedAt = storedUpdatedAt(notificationId);

		var outcome = apply(MESSAGE_ID, NotificationStatus.SUBMITTED, null);

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.IGNORED);
		assertStored(notificationId, NotificationStatus.SUBMITTED, MESSAGE_ID);
		assertThat(storedUpdatedAt(notificationId)).isEqualTo(updatedAt);
	}

	@Test
	void outOfOrderCallbackDoesNotRegressFinalState() {
		var notificationId = committedNotification(notification -> notification.markAccepted(MESSAGE_ID));
		assertThat(apply(MESSAGE_ID, NotificationStatus.DELIVERED, null)).isEqualTo(StatusCallbackOutcome.APPLIED);

		var outcome = apply(MESSAGE_ID, NotificationStatus.SUBMITTED, null);

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.IGNORED);
		assertStored(notificationId, NotificationStatus.DELIVERED, MESSAGE_ID);
	}

	@Test
	void callbackDoesNotChangeFailedRequest() {
		var notificationId = committedNotification(
			notification -> notification.markRequestFailed(NotificationFailureReason.PROVIDER_REQUEST_REJECTED));

		var outcome = apply(MESSAGE_ID, NotificationStatus.DELIVERED, notificationId.toString());

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.IGNORED);
		assertStored(notificationId, NotificationStatus.REQUEST_FAILED, null,
			NotificationFailureReason.PROVIDER_REQUEST_REJECTED);
	}

	@Test
	void callbackDoesNotChangeNotRequestedNotification() {
		var notificationId = committedNotification(ReservationNotification::notRequestedCannotReceive,
			notification -> { });

		var outcome = apply(MESSAGE_ID, NotificationStatus.DELIVERED, notificationId.toString());

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.IGNORED);
		assertStored(notificationId, NotificationStatus.NOT_REQUESTED_CANNOT_RECEIVE, null);
	}

	@ParameterizedTest
	@EnumSource(value = NotificationStatus.class, names = { "REJECTED", "UNDELIVERABLE" })
	void deliveryFailureKeepsReservationConfirmed(NotificationStatus failure) {
		var notificationId = committedNotification(notification -> notification.markAccepted(MESSAGE_ID));

		var outcome = apply(MESSAGE_ID, failure, null);

		assertThat(outcome).isEqualTo(StatusCallbackOutcome.APPLIED);
		assertStored(notificationId, failure, MESSAGE_ID);
		assertThat(jdbcTemplate.queryForObject(
			"SELECT status FROM reservations WHERE id = ?", String.class, reservationIds.getFirst()))
			.isEqualTo("CONFIRMED");
	}

	@Test
	void concurrentCallbacksForPendingNotificationAreAppliedOneAfterTheOther() throws Exception {
		var notificationId = committedNotification(notification -> { });
		var clientRef = notificationId.toString();

		var outcomes = applyWhileFirstHoldsLock(
			new MessagesStatusCallback(MESSAGE_ID, NotificationStatus.SUBMITTED, clientRef),
			new MessagesStatusCallback(MESSAGE_ID, NotificationStatus.DELIVERED, clientRef));

		assertThat(outcomes).containsExactly(StatusCallbackOutcome.APPLIED, StatusCallbackOutcome.APPLIED);
		assertStored(notificationId, NotificationStatus.DELIVERED, MESSAGE_ID);
	}

	@Test
	void concurrentDuplicateCallbacksApplyOnlyOnce() throws Exception {
		var notificationId = committedNotification(notification -> { });
		var callback = new MessagesStatusCallback(MESSAGE_ID, NotificationStatus.SUBMITTED, notificationId.toString());

		var outcomes = applyWhileFirstHoldsLock(callback, callback);

		assertThat(outcomes).containsExactly(StatusCallbackOutcome.APPLIED, StatusCallbackOutcome.IGNORED);
		assertStored(notificationId, NotificationStatus.SUBMITTED, MESSAGE_ID);
	}

	private StatusCallbackOutcome apply(String messageUuid, NotificationStatus status, String clientRef) {
		return callbackService.apply(new MessagesStatusCallback(messageUuid, status, clientRef));
	}

	private UUID committedNotification(Consumer<ReservationNotification> transition) {
		return committedNotification(ReservationNotification::pending, transition);
	}

	private UUID committedNotification(Function<UUID, ReservationNotification> factory,
									   Consumer<ReservationNotification> transition) {
		var transaction = new TransactionTemplate(transactionManager);
		return transaction.execute(status -> {
			var reservation = reservationRepository.save(new Reservation(
				"Fictional",
				"Guest",
				"fictional-test-contact",
				LocalDate.of(2027, 5, 14),
				LocalTime.of(13, 30),
				DiningSession.LUNCH,
				(short) 4));
			reservationIds.add(reservation.getId());
			var notification = notificationRepository.save(factory.apply(reservation.getId()));
			transition.accept(notification);
			return notification.getId();
		});
	}

	private void assertStored(UUID notificationId, NotificationStatus status, String messageId) {
		assertStored(notificationId, status, messageId, null);
	}

	private void assertStored(UUID notificationId, NotificationStatus status, String messageId,
							  NotificationFailureReason failureReason) {
		var stored = jdbcTemplate.queryForMap(
			"SELECT status, provider_message_id, failure_reason FROM reservation_notifications WHERE id = ?",
			notificationId);
		assertThat(stored)
			.containsEntry("status", status.name())
			.containsEntry("provider_message_id", messageId)
			.containsEntry("failure_reason", failureReason == null ? null : failureReason.name());
	}

	private Object storedUpdatedAt(UUID notificationId) {
		return jdbcTemplate.queryForObject(
			"SELECT updated_at FROM reservation_notifications WHERE id = ?", Object.class, notificationId);
	}

	private List<StatusCallbackOutcome> applyWhileFirstHoldsLock(MessagesStatusCallback first,
																 MessagesStatusCallback second) throws Exception {
		var transaction = new TransactionTemplate(transactionManager);
		var firstHoldsLock = new CountDownLatch(1);
		var releaseFirst = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		try {
			Future<StatusCallbackOutcome> firstResult = executor.submit(() -> transaction.execute(status -> {
				var outcome = callbackService.apply(first);
				firstHoldsLock.countDown();
				await(releaseFirst);
				return outcome;
			}));
			assertThat(firstHoldsLock.await(10, TimeUnit.SECONDS)).isTrue();

			Future<StatusCallbackOutcome> secondResult = executor.submit(() -> callbackService.apply(second));

			assertThatThrownBy(() -> secondResult.get(500, TimeUnit.MILLISECONDS))
				.isInstanceOf(TimeoutException.class);

			releaseFirst.countDown();
			return List.of(firstResult.get(10, TimeUnit.SECONDS), secondResult.get(10, TimeUnit.SECONDS));
		} finally {
			executor.shutdownNow();
		}
	}

	private static void await(CountDownLatch latch) {
		try {
			if (!latch.await(10, TimeUnit.SECONDS)) {
				throw new IllegalStateException("Timed out waiting for the test to release the lock");
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(exception);
		}
	}
}
