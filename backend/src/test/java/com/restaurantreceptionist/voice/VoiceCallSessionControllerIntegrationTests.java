package com.restaurantreceptionist.voice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.restaurantreceptionist.TestcontainersConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class VoiceCallSessionControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private VoiceCallSessionRepository repository;

	@Test
	void answerCreatesActiveSessionAndReturnsGreetingNcco() throws Exception {
		var uuid = UUID.randomUUID().toString();

		mockMvc.perform(post("/api/webhooks/vonage/voice/answer")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uuid\":\"" + uuid + "\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].action").value("talk"))
			.andExpect(jsonPath("$[0].language").value("es-ES"));

		assertThat(repository.findByUuid(uuid).orElseThrow().getState()).isEqualTo(CallState.ACTIVE);
	}

	@Test
	void completedEventTerminatesActiveSession() throws Exception {
		var uuid = UUID.randomUUID().toString();
		mockMvc.perform(post("/api/webhooks/vonage/voice/answer")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uuid\":\"" + uuid + "\"}"))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/webhooks/vonage/voice/event")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uuid\":\"" + uuid + "\",\"status\":\"completed\"}"))
			.andExpect(status().isNoContent());

		assertThat(repository.findByUuid(uuid).orElseThrow().getState()).isEqualTo(CallState.ABANDONED);
	}

	@Test
	void answerAfterTerminalEventDoesNotReopenSession() throws Exception {
		var uuid = UUID.randomUUID().toString();
		mockMvc.perform(post("/api/webhooks/vonage/voice/event")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uuid\":\"" + uuid + "\",\"status\":\"completed\"}"))
			.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/webhooks/vonage/voice/answer")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uuid\":\"" + uuid + "\"}"))
			.andExpect(status().isNoContent());

		assertThat(repository.findByUuid(uuid).orElseThrow().getState()).isEqualTo(CallState.ABANDONED);
	}
}
