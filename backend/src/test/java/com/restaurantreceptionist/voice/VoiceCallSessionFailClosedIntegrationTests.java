package com.restaurantreceptionist.voice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.restaurantreceptionist.TestcontainersConfiguration;
import com.restaurantreceptionist.webhooks.VonageWebhookFixtures;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "vonage.webhooks.enabled=false")
@AutoConfigureMockMvc
class VoiceCallSessionFailClosedIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsEvenValidlySignedRequestsWhileVerificationIsDisabled() throws Exception {
		var body = "{\"uuid\":\"call-leg-uuid\"}";

		mockMvc.perform(post("/api/webhooks/vonage/voice/answer")
			.header(HttpHeaders.AUTHORIZATION, VonageWebhookFixtures.authorizationFor(body))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isServiceUnavailable());
	}
}
