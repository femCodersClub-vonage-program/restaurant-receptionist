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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.restaurantreceptionist.notifications.ReservationNotification;
import com.restaurantreceptionist.notifications.ReservationNotificationRepository;
import com.restaurantreceptionist.reservations.DiningSession;
import com.restaurantreceptionist.reservations.Reservation;
import com.restaurantreceptionist.reservations.ReservationRepository;
import com.restaurantreceptionist.webhooks.VonageWebhookFixtures;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
	"vonage.webhooks.enabled=true",
	"vonage.webhooks.api-key=" + VonageWebhookFixtures.API_KEY,
	"vonage.webhooks.signature-secret=" + VonageWebhookFixtures.SIGNATURE_SECRET,
	"vonage.webhooks.application-id=" + VonageWebhookFixtures.APPLICATION_ID })
@AutoConfigureMockMvc
class MessagesStatusWebhookIntegrationTests {

	private static final String URL = "/api/webhooks/vonage/messages/status";

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
	void signedStatusIsCommittedBeforeTheOkResponse() throws Exception {
		var notificationId = committedAcceptedNotification();
		var body = callback(notificationId, "delivered");

		send(body, VonageWebhookFixtures.authorizationFor(body)).andExpect(status().isOk());

		assertThat(storedStatus(notificationId)).isEqualTo("DELIVERED");
	}

	@Test
	void callbackWhoseBodyDiffersFromTheSignedOneIsRejectedWithoutChanges() throws Exception {
		var notificationId = committedAcceptedNotification();
		var signedBody = callback(notificationId, "delivered");

		send(callback(notificationId, "rejected"), VonageWebhookFixtures.authorizationFor(signedBody))
			.andExpect(status().isUnauthorized());

		assertThat(storedStatus(notificationId)).isEqualTo("ACCEPTED");
	}

	private ResultActions send(String body, String authorization) throws Exception {
		return mockMvc.perform(post(URL)
			.header(HttpHeaders.AUTHORIZATION, authorization)
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}

	private static String callback(UUID notificationId, String status) {
		return """
			{"message_uuid":"%s","status":"%s","channel":"sms","client_ref":"%s"}
			""".formatted(MESSAGE_ID, status, notificationId);
	}

	private String storedStatus(UUID notificationId) {
		return jdbcTemplate.queryForObject(
			"SELECT status FROM reservation_notifications WHERE id = ?", String.class, notificationId);
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
