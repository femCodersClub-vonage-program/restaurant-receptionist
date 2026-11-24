package com.restaurantreceptionist.voice;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurantreceptionist.voice.AnswerResult.AlreadyTerminal;
import com.restaurantreceptionist.voice.AnswerResult.Opened;
import com.restaurantreceptionist.web.ProblemDetails;

@RestController
@RequestMapping("/api/webhooks/vonage/voice")
public class VoiceCallSessionController {

	private final VoiceCallSessionService voiceCallSessionService;

	private final SignatureVerifier signatureVerifier;

	public VoiceCallSessionController(VoiceCallSessionService voiceCallSessionService,
			SignatureVerifier signatureVerifier) {
		this.voiceCallSessionService = voiceCallSessionService;
		this.signatureVerifier = signatureVerifier;
	}

	@PostMapping(value = "/answer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> answer(@RequestBody AnswerRequest request, HttpServletRequest httpRequest) {
		Optional<ResponseEntity<Void>> rejected = rejected(httpRequest);
		if (rejected.isPresent()) {
			return rejected.get();
		}
		if (request.uuid() == null || request.uuid().isBlank()) {
			return malformed("uuid", "MISSING");
		}

		return switch (voiceCallSessionService.answer(request.uuid(), request.conversationUuid())) {
			case Opened opened -> ResponseEntity.ok(List.of(opened.ncco()));
			case AlreadyTerminal ignored -> ResponseEntity.noContent().build();
		};
	}

	@PostMapping(value = "/event", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> event(@RequestBody CallEvent event, HttpServletRequest httpRequest) {
		Optional<ResponseEntity<Void>> rejected = rejected(httpRequest);
		if (rejected.isPresent()) {
			return rejected.get();
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

	private Optional<ResponseEntity<Void>> rejected(HttpServletRequest request) {
		return switch (signatureVerifier.verify(request)) {
			case SignatureVerification.Missing ignored ->
				Optional.of(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
			case SignatureVerification.Invalid ignored ->
				Optional.of(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
			case SignatureVerification.Verified ignored -> Optional.empty();
		};
	}

	private static ResponseEntity<ProblemDetail> malformed(String field, String reason) {
		ProblemDetail problem = ProblemDetails.forStatusAndDetail(
			HttpStatus.BAD_REQUEST, "The answer webhook is missing required documented fields.");
		problem.setProperty("field", field);
		problem.setProperty("reason", reason);
		return ResponseEntity.badRequest().body(problem);
	}
}
