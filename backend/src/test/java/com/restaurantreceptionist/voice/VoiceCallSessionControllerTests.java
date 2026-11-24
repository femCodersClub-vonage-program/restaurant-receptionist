package com.restaurantreceptionist.voice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VoiceCallSessionController.class)
class VoiceCallSessionControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SignatureVerifier signatureVerifier;

	@MockitoBean
	private VoiceCallSessionService voiceCallSessionService;

	@Test
	void answerRejectsMissingSignatureWithServiceUnavailable() throws Exception {
		when(signatureVerifier.verify(any())).thenReturn(new SignatureVerification.Missing());

		mockMvc.perform(post("/api/webhooks/vonage/voice/answer")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uuid\":\"call-leg-uuid\"}"))
			.andExpect(status().isServiceUnavailable());

		verifyNoInteractions(voiceCallSessionService);
	}

	@Test
	void eventRejectsInvalidSignatureWithUnauthorized() throws Exception {
		when(signatureVerifier.verify(any())).thenReturn(new SignatureVerification.Invalid());

		mockMvc.perform(post("/api/webhooks/vonage/voice/event")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uuid\":\"call-leg-uuid\",\"status\":\"answered\"}"))
			.andExpect(status().isUnauthorized());

		verifyNoInteractions(voiceCallSessionService);
	}

	@Test
	void answerRejectsMissingUuidWithBadRequestWhenVerified() throws Exception {
		when(signatureVerifier.verify(any())).thenReturn(new SignatureVerification.Verified());

		mockMvc.perform(post("/api/webhooks/vonage/voice/answer")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.field").value("uuid"));

		verifyNoInteractions(voiceCallSessionService);
	}

	@Test
	void answerReturnsGreetingNccoWhenVerified() throws Exception {
		when(signatureVerifier.verify(any())).thenReturn(new SignatureVerification.Verified());
		when(voiceCallSessionService.answer("call-leg-uuid", null))
			.thenReturn(new AnswerResult.Opened(new Ncco("talk", "Hola", "es-ES")));

		mockMvc.perform(post("/api/webhooks/vonage/voice/answer")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uuid\":\"call-leg-uuid\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].action").value("talk"));
	}

	@Test
	void eventAcknowledgesWhenVerified() throws Exception {
		when(signatureVerifier.verify(any())).thenReturn(new SignatureVerification.Verified());
		when(voiceCallSessionService.handleEvent(any())).thenReturn(new CallEventResult.Acknowledged());

		mockMvc.perform(post("/api/webhooks/vonage/voice/event")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uuid\":\"call-leg-uuid\",\"status\":\"answered\"}"))
			.andExpect(status().isNoContent());
	}

	@Test
	void answerReturnsInternalServerErrorWhenSessionCannotBeLocated() throws Exception {
		when(signatureVerifier.verify(any())).thenReturn(new SignatureVerification.Verified());
		when(voiceCallSessionService.answer("call-leg-uuid", null))
			.thenThrow(new VoiceCallSessionNotFoundException("call-leg-uuid"));

		mockMvc.perform(post("/api/webhooks/vonage/voice/answer")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uuid\":\"call-leg-uuid\"}"))
			.andExpect(status().isInternalServerError());
	}
}
