package com.restaurantreceptionist.notifications;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.restaurantreceptionist.webhooks.WebhookSignatureVerification;
import com.restaurantreceptionist.webhooks.WebhookSignatureVerifier;

@WebMvcTest(MessagesStatusWebhookController.class)
class MessagesStatusWebhookControllerTests {

	private static final String URL = "/api/webhooks/vonage/messages/status";

	private static final String AUTHORIZATION = "Bearer fictional-token";

	private static final String MESSAGE_UUID = "aaaaaaaa-bbbb-cccc-dddd-0123456789ab";

	private static final String CLIENT_REF = "00000000-0000-0000-0000-000000000001";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private WebhookSignatureVerifier signatureVerifier;

	@MockitoBean
	private NotificationStatusCallbackService callbackService;

	@BeforeEach
	void verifiedByDefault() {
		when(signatureVerifier.verify(any(), any())).thenReturn(WebhookSignatureVerification.VERIFIED);
	}

	@Test
	void appliesDocumentedSmsCallbackAndAcknowledgesWithEmptyOk()  throws Exception {
		var body = fixture("sms-delivered.json");
		when(callbackService.apply(any())).thenReturn(StatusCallbackOutcome.APPLIED);

		send(body)
			.andExpect(status().isOk())
			.andExpect(content().string(""));
		verify(signatureVerifier).verify(AUTHORIZATION, body);
		verify(callbackService).apply(
			new MessagesStatusCallback(MESSAGE_UUID, NotificationStatus.DELIVERED, CLIENT_REF));
	}

	@Test
	void rejectsMissingSignatureBeforeReadingTheBody() throws Exception {
		when(signatureVerifier.verify(any(), any())).thenReturn(WebhookSignatureVerification.MISSING_SIGNATURE);

		send("not json").andExpect(status().isServiceUnavailable());

		verifyNoInteractions(callbackService);
	}

	@Test
	void rejectsInvalidSignatureBeforeReadingTheBody() throws Exception {
		when(signatureVerifier.verify(any(), any())).thenReturn(WebhookSignatureVerification.INVALID_SIGNATURE);

		send("not json").andExpect(status().isUnauthorized());

		verifyNoInteractions(callbackService);
	}

	@Test
	void passesAbsentHeaderAsNullAndAbsentBodyAsEmptyString() throws Exception {
		when(signatureVerifier.verify(any(), any())).thenReturn(WebhookSignatureVerification.MISSING_SIGNATURE);

		mockMvc.perform(post(URL)).andExpect(status().isServiceUnavailable());

		verify(signatureVerifier).verify(null, "");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"",
		"   ",
		"not json",
		"null",
		"[]",
		"{\"status\":\"delivered\",\"channel\":\"sms\"}",
		"{\"message_uuid\":\" \",\"status\":\"delivered\",\"channel\":\"sms\"}",
		"{\"message_uuid\":\"m\",\"channel\":\"sms\"}",
		"{\"message_uuid\":\"m\",\"status\":\"\",\"channel\":\"sms\"}",
		"{\"message_uuid\":\"m\",\"status\":\"delivered\"}",
		"{\"message_uuid\":\"m\",\"status\":\"delivered\",\"channel\":\" \"}",
		"{\"message_uuid\":\"m\",\"status\":123,\"channel\":\"sms\"}",
		"{\"message_uuid\":\"m\",\"status\":\"delivered\",\"channel\":\"sms\",\"client_ref\":42}"
	})
	void rejectsMalformedCallbacksWithProblemDetail(String body) throws Exception {
		send(body)
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

		verifyNoInteractions(callbackService);
	}

	@ParameterizedTest
	@ValueSource(strings = { "whatsapp", "rcs", "SMS" })
	void ignoresCallbacksForOtherChannels(String channel) throws Exception {
		send(callback("delivered", channel))
			.andExpect(status().isOk())
			.andExpect(content().string(""));

		verifyNoInteractions(callbackService);
	}

	@ParameterizedTest
	@ValueSource(strings = { "read", "accepted", "DELIVERED" })
	void ignoresUnsupportedStatuses(String vonageStatus) throws Exception {
		send(callback(vonageStatus, "sms"))
			.andExpect(status().isOk())
			.andExpect(content().string(""));

		verifyNoInteractions(callbackService);
	}

	@ParameterizedTest
	@EnumSource(StatusCallbackOutcome.class)
	void acknowledgesEveryServiceOutcomeWithEmptyOk(StatusCallbackOutcome outcome) throws Exception {
		when(callbackService.apply(any())).thenReturn(outcome);

		send(callback("submitted", "sms"))
			.andExpect(status().isOk())
			.andExpect(content().string(""));
	}

	@Test
	void returnsServerErrorWhenProcessingFails() throws Exception {
		when(callbackService.apply(any())).thenThrow(new IllegalStateException("simulated failure"));

		send(callback("delivered", "sms"))
			.andExpect(status().isInternalServerError())
			.andExpect(content().string(""));
	}

	private ResultActions send(String body) throws Exception {
		return mockMvc.perform(post(URL)
			.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION)
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}

	private static String callback(String status, String channel) {
		return """
			{"message_uuid":"%s","status":"%s","channel":"%s","client_ref":"%s"}
			""".formatted(MESSAGE_UUID, status, channel, CLIENT_REF);
	}

	private static String fixture(String name) throws IOException {
		return new ClassPathResource("vonage/messages/" + name).getContentAsString(StandardCharsets.UTF_8);
	}

}
