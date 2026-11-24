package com.restaurantreceptionist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.restaurantreceptionist.notifications.NotificationStatus;
import com.restaurantreceptionist.notifications.ReservationNotification;
import com.restaurantreceptionist.notifications.ReservationNotificationRepository;
import com.restaurantreceptionist.reservations.DiningSession;
import com.restaurantreceptionist.reservations.Reservation;
import com.restaurantreceptionist.reservations.ReservationRepository;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class NotificationConcurrencyIntegrationTests {

	private static final String MESSAGE_ID = "message-1";

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private ReservationNotificationRepository notificationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private UUID reservationId;

	@AfterEach
	void tearDown() {
		if (reservationId != null) {
			jdbcTemplate.update("DELETE FROM reservation_notifications WHERE reservation_id = ?", reservationId);
			jdbcTemplate.update("DELETE FROM reservations WHERE id = ?", reservationId);
		}
	}

	@Test
	void lateAcceptanceWaitsForCallbackAndKeepsProviderStatus() throws Exception {
		var notificationId = committedPendingNotification();

		var results = applyWhileFirstHoldsLock(notificationId,
			notification -> notification.applyProviderStatus(NotificationStatus.DELIVERED, MESSAGE_ID),
			notification -> notification.markAccepted(MESSAGE_ID));

		assertThat(results).containsExactly(true, false);
		assertStored(notificationId, NotificationStatus.DELIVERED, MESSAGE_ID);
	}

	@Test
	void callbackWaitsForAcceptanceAndAppliesProviderStatus() throws Exception {
		var notificationId = committedPendingNotification();

		var results = applyWhileFirstHoldsLock(notificationId,
			notification -> notification.markAccepted(MESSAGE_ID),
			notification -> notification.applyProviderStatus(NotificationStatus.DELIVERED, MESSAGE_ID));

		assertThat(results).containsExactly(true, true);
		assertStored(notificationId, NotificationStatus.DELIVERED, MESSAGE_ID);
	}

	private UUID committedPendingNotification() {
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
			reservationId = reservation.getId();
			return
				notificationRepository.save(ReservationNotification.pending(reservationId)).getId();
		});
	}

	private List<Boolean> applyWhileFirstHoldsLock(UUID notificationId,
												   Function<ReservationNotification, Boolean> first,
												   Function<ReservationNotification, Boolean> second) throws Exception {
		var transaction = new TransactionTemplate(transactionManager);
		var firstHoldsLock = new CountDownLatch(1);
		var releaseFirst = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		try {
			Future<Boolean> firstResult = executor.submit(() -> transaction.execute(status -> {
				var notification = notificationRepository.findForUpdateById(notificationId).orElseThrow();
				boolean changed = first.apply(notification);
				firstHoldsLock.countDown();
				await(releaseFirst);
				return changed;
			}));
			assertThat(firstHoldsLock.await(10, TimeUnit.SECONDS)).isTrue();

			Future<Boolean> secondResult = executor.submit(() -> transaction.execute(status ->
				second.apply(notificationRepository.findForUpdateById(notificationId).orElseThrow())));

			assertThatThrownBy(() -> secondResult.get(500, TimeUnit.MILLISECONDS))
				.isInstanceOf(TimeoutException.class);

			releaseFirst.countDown();
			return List.of(firstResult.get(10, TimeUnit.SECONDS), secondResult.get(10, TimeUnit.SECONDS));
		} finally {
			executor.shutdownNow();
		}
	}

	private void assertStored(UUID notificationId, NotificationStatus status, String messageId) {
		var stored = jdbcTemplate.queryForMap(
			"SELECT status, provider_message_id FROM reservation_notifications WHERE id = ?", notificationId);
		assertThat(stored)
			.containsEntry("status", status.name())
			.containsEntry("provider_message_id", messageId);
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
