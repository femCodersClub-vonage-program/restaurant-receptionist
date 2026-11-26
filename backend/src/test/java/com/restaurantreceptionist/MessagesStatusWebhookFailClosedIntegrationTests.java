package com.restaurantreceptionist;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.restaurantreceptionist.webhooks.VonageWebhookFixtures;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "vonage.webhooks.enabled=false")
@AutoConfigureMockMvc
class MessagesStatusWebhookFailClosedIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsEvenValidlySignedCallbacksWhileVerificationIsDisabled() throws Exception {
		var body = """
			{"message_uuid":"fictional-message-1","status":"delivered","channel":"sms"}
			""";

		mockMvc.perform(post("/api/webhooks/vonage/messages/status")
			.header(HttpHeaders.AUTHORIZATION, VonageWebhookFixtures.authorizationFor(body))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isServiceUnavailable());
	}
}
