package com.restaurantreceptionist.voice;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurantreceptionist.voice.AnswerResult.AlreadyTerminal;
import com.restaurantreceptionist.voice.AnswerResult.Opened;
import com.restaurantreceptionist.web.ProblemDetails;
import com.restaurantreceptionist.webhooks.WebhookSignatureVerifier;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api/webhooks/vonage/voice")
public class VoiceCallSessionController {

	private static final String PROBLEM_JSON = "application/problem+json";

	private final VoiceCallSessionService voiceCallSessionService;

	private final WebhookSignatureVerifier signatureVerifier;

	private final JsonMapper jsonMapper;

	public VoiceCallSessionController(VoiceCallSessionService voiceCallSessionService,
			WebhookSignatureVerifier signatureVerifier, JsonMapper jsonMapper) {
		this.voiceCallSessionService = voiceCallSessionService;
		this.signatureVerifier = signatureVerifier;
		this.jsonMapper = jsonMapper;
	}

	@PostMapping(value = "/answer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Receive a Vonage Voice answer webhook")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Greeting NCCO for a new or repeated active session"),
		@ApiResponse(responseCode = "204", description = "Answer on a terminal session; the call is not reopened"),
		@ApiResponse(
			responseCode = "400",
			description = "Missing or blank uuid, or an unreadable body",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
		@ApiResponse(responseCode = "401", description = "Invalid signature"),
		@ApiResponse(responseCode = "503", description = "Missing signature; Vonage may retry"),
		@ApiResponse(responseCode = "500", description = "Unexpected error; Vonage may retry")
	})
	public ResponseEntity<?> answer(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@RequestBody(required = false) String rawBody) {
		Optional<ResponseEntity<Void>> rejected = rejected(authorization, rawBody);
		if (rejected.isPresent()) {
			return rejected.get();
		}

		AnswerRequest request = parse(rawBody, AnswerRequest.class);
		if (request == null || request.uuid() == null || request.uuid().isBlank()) {
			return malformed("uuid", "MISSING");
		}

		return switch (voiceCallSessionService.answer(request.uuid(), request.conversationUuid())) {
			case Opened opened -> ResponseEntity.ok(List.of(opened.ncco()));
			case AlreadyTerminal ignored -> ResponseEntity.noContent().build();
		};
	}

	@PostMapping(value = "/event", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Receive a Vonage Voice event webhook")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "Event acknowledged, whether applied or ignored"),
		@ApiResponse(
			responseCode = "400",
			description = "Unreadable event payload",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
		@ApiResponse(responseCode = "401", description = "Invalid signature"),
		@ApiResponse(responseCode = "503", description = "Missing signature; Vonage may retry"),
		@ApiResponse(responseCode = "500", description = "Unexpected error; Vonage may retry")
	})
	public ResponseEntity<?> event(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@RequestBody(required = false) String rawBody) {
		Optional<ResponseEntity<Void>> rejected = rejected(authorization, rawBody);
		if (rejected.isPresent()) {
			return rejected.get();
		}

		CallEvent event = parse(rawBody, CallEvent.class);
		if (event == null) {
			return ResponseEntity.badRequest().body(
				ProblemDetails.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The event webhook is not readable."));
		}

		voiceCallSessionService.handleEvent(event);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(VoiceCallSessionNotFoundException.class)
	ResponseEntity<ProblemDetail> handleSessionNotFound(VoiceCallSessionNotFoundException exception) {
		ProblemDetail problem = ProblemDetails.forStatusAndDetail(
			HttpStatus.INTERNAL_SERVER_ERROR, "The call session could not be located after creation.");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
	}

	private Optional<ResponseEntity<Void>> rejected(String authorization, String rawBody) {
		String body = Objects.requireNonNullElse(rawBody, "");
		return switch (signatureVerifier.verify(authorization, body)) {
			case MISSING_SIGNATURE -> Optional.of(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
			case INVALID_SIGNATURE -> Optional.of(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
			case VERIFIED -> Optional.empty();
		};
	}

	private <T> T parse(String rawBody, Class<T> type) {
		if (rawBody == null || rawBody.isBlank()) {
			return null;
		}
		try {
			return jsonMapper.readValue(rawBody, type);
		} catch (JacksonException exception) {
			return null;
		}
	}

	private static ResponseEntity<ProblemDetail> malformed(String field, String reason) {
		ProblemDetail problem = ProblemDetails.forStatusAndDetail(
			HttpStatus.BAD_REQUEST, "The answer webhook is missing required documented fields.");
		problem.setProperty("field", field);
		problem.setProperty("reason", reason);
		return ResponseEntity.badRequest().body(problem);
	}
}
