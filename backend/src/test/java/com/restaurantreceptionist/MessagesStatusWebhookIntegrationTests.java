package com.restaurantreceptionist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.restaurantreceptionist.notifications.ReservationNotification;
import com.restaurantreceptionist.notifications.ReservationNotificationRepository;
import com.restaurantreceptionist.notifications.VerifiedMessagesSignatureTestConfiguration;
import com.restaurantreceptionist.reservations.DiningSession;
import com.restaurantreceptionist.reservations.Reservation;
import com.restaurantreceptionist.reservations.ReservationRepository;

@Import({ TestcontainersConfiguration.class, VerifiedMessagesSignatureTestConfiguration.class })
@SpringBootTest
@AutoConfigureMockMvc
class MessagesStatusWebhookIntegrationTests {

	private static final String MESSAGE_ID = "fictional-message-1";

	@Autowired
	private MockMvc mockMvc;

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
	void appliedStatusIsCommittedBeforeTheOkResponse() throws Exception {
		var notificationId = committedAcceptedNotification();

		mockMvc.perform(post("/api/webhooks/vonage/messages/status")
			.header(HttpHeaders.AUTHORIZATION, "Bearer fictional-token")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{"message_uuid":"%s","status":"delivered","channel":"sms","client_ref":"%s"}
				""".formatted(MESSAGE_ID, notificationId)))
			.andExpect(status().isOk());

		assertThat(jdbcTemplate.queryForObject(
			"SELECT status FROM reservation_notifications WHERE id = ?", String.class, notificationId))
			.isEqualTo("DELIVERED");
	}

	private UUID committedAcceptedNotification() {
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
			var notification = notificationRepository.save(ReservationNotification.pending(reservationId));
			notification.markAccepted(MESSAGE_ID);
			return notification.getId();
		});
	}
}
