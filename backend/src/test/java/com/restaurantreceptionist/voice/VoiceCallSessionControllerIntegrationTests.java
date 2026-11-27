package com.restaurantreceptionist.voice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
import org.springframework.test.web.servlet.ResultActions;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
	"vonage.webhooks.enabled=true",
	"vonage.webhooks.api-key=" + VonageWebhookFixtures.API_KEY,
	"vonage.webhooks.signature-secret=" + VonageWebhookFixtures.SIGNATURE_SECRET,
	"vonage.webhooks.application-id=" + VonageWebhookFixtures.APPLICATION_ID })
@AutoConfigureMockMvc
class VoiceCallSessionControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private VoiceCallSessionRepository repository;

	@Test
	void answerCreatesActiveSessionAndReturnsGreetingNcco() throws Exception {
		var uuid = UUID.randomUUID().toString();

		send("/api/webhooks/vonage/voice/answer", "{\"uuid\":\"" + uuid + "\"}")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].action").value("talk"))
			.andExpect(jsonPath("$[0].language").value("es-ES"));

		assertThat(repository.findByUuid(uuid).orElseThrow().getState()).isEqualTo(CallState.ACTIVE);
	}

	@Test
	void completedEventTerminatesActiveSession() throws Exception {
		var uuid = UUID.randomUUID().toString();
		send("/api/webhooks/vonage/voice/answer", "{\"uuid\":\"" + uuid + "\"}").andExpect(status().isOk());

		send("/api/webhooks/vonage/voice/event", "{\"uuid\":\"" + uuid + "\",\"status\":\"completed\"}")
			.andExpect(status().isNoContent());

		assertThat(repository.findByUuid(uuid).orElseThrow().getState()).isEqualTo(CallState.ABANDONED);
	}

	@Test
	void answerAfterTerminalEventDoesNotReopenSession() throws Exception {
		var uuid = UUID.randomUUID().toString();
		send("/api/webhooks/vonage/voice/event", "{\"uuid\":\"" + uuid + "\",\"status\":\"completed\"}")
			.andExpect(status().isNoContent());

		send("/api/webhooks/vonage/voice/answer", "{\"uuid\":\"" + uuid + "\"}").andExpect(status().isNoContent());

		assertThat(repository.findByUuid(uuid).orElseThrow().getState()).isEqualTo(CallState.ABANDONED);
	}

	@Test
	void answerPersistsConversationUuid() throws Exception {
		var uuid = UUID.randomUUID().toString();

		send("/api/webhooks/vonage/voice/answer", "{\"uuid\":\"" + uuid + "\",\"conversation_uuid\":\"conversation-123\"}")
			.andExpect(status().isOk());

		assertThat(repository.findByUuid(uuid).orElseThrow().getConversationUuid()).isEqualTo("conversation-123");
	}

	@Test
	void completedEventPersistsDisconnectedBy() throws Exception {
		var uuid = UUID.randomUUID().toString();
		send("/api/webhooks/vonage/voice/answer", "{\"uuid\":\"" + uuid + "\"}").andExpect(status().isOk());

		send("/api/webhooks/vonage/voice/event",
			"{\"uuid\":\"" + uuid + "\",\"status\":\"completed\",\"disconnected_by\":\"user\"}")
			.andExpect(status().isNoContent());

		var stored = repository.findByUuid(uuid).orElseThrow();
		assertThat(stored.getState()).isEqualTo(CallState.ABANDONED);
		assertThat(stored.getDisconnectedBy()).isEqualTo(DisconnectedBy.CALLER);
	}

	private ResultActions send(String url, String body) throws Exception {
		return mockMvc.perform(post(url)
			.header(HttpHeaders.AUTHORIZATION, VonageWebhookFixtures.authorizationFor(body))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}
}
