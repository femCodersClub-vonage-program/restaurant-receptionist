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
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class MessagesStatusWebhookFailClosedIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsEveryCallbackUntilTheRealVerifierIsAvailable() throws Exception {
		mockMvc.perform(post("/api/webhooks/vonage/messages/status")
			.header(HttpHeaders.AUTHORIZATION, "Bearer fictional-token")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{"message_uuid":"fictional-message-1","status":"delivered","channel":"sms"}
				"""))
			.andExpect(status().isServiceUnavailable());
	}
}
