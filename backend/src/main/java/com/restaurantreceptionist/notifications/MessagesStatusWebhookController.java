package com.restaurantreceptionist.notifications;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api/webhooks/vonage/messages")
class MessagesStatusWebhookController {

	private static final Logger log = LoggerFactory.getLogger(MessagesStatusWebhookController.class);

	private static final String PROBLEM_JSON = "application/problem+json";

	private static final String SMS_CHANNEL = "sms";

	private final MessagesSignatureVerifier signatureVerifier;

	private final NotificationStatusCallbackService callbackService;

	private final JsonMapper jsonMapper;

	MessagesStatusWebhookController(
		MessagesSignatureVerifier signatureVerifier,
		NotificationStatusCallbackService callbackService,
		JsonMapper jsonMapper) {
		this.signatureVerifier = signatureVerifier;
		this.callbackService = callbackService;
		this.jsonMapper = jsonMapper;
	}

	@PostMapping("/status")
	@Operation(summary = "Receive a Vonage Messages status callback")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Callback accepted, whether applied or ignored"),
		@ApiResponse(
			responseCode = "400",
			description = "Malformed JSON or a missing, blank or wrongly typed required field",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
		@ApiResponse(responseCode = "401", description = "Invalid signature"),
		@ApiResponse(responseCode = "503", description = "Missing signature; Vonage may retry"),
		@ApiResponse(responseCode = "500", description = "Unexpected error; Vonage may retry")
	})
	ResponseEntity<?> receiveStatus(
		@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
		@RequestBody(required = false) String rawBody) {

		Optional<ResponseEntity<Void>> rejected = rejected(signatureVerifier.verify(authorization, rawBody));
		if (rejected.isPresent()) {
			return rejected.get();
		}

		Optional<MessagesStatusRequest> request = parse(rawBody);
		if (request.isEmpty() || !hasRequiredFields(request.get())) {
			return malformed();
		}

		MessagesStatusRequest callback = request.get();
		if (!SMS_CHANNEL.equals(callback.channel())) {
			log.info("Messages status callback ignored: NON_SMS_CHANNEL");
			return ResponseEntity.ok().build();
		}

		Optional<NotificationStatus> status = MessagesStatusMapping.toNotificationStatus(callback.status());
		if (status.isEmpty()) {
			log.info("Messages status callback ignored: UNSUPPORTED_STATUS");
			return ResponseEntity.ok().build();
		}

		callbackService.apply(new MessagesStatusCallback(callback.messageUuid(), status.get(), callback.clientRef()));
		return ResponseEntity.ok().build();
	}

	@ExceptionHandler(RuntimeException.class)
	ResponseEntity<Void> handleUnexpectedError(RuntimeException exception) {
		log.error("Messages status callback failed: {}", exception.getClass().getSimpleName());
		return ResponseEntity.internalServerError().build();
	}

	private static Optional<ResponseEntity<Void>> rejected(MessagesSignatureVerification verification) {
		return switch (verification) {
			case MessagesSignatureVerification.Missing ignored ->
				Optional.of(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
			case MessagesSignatureVerification.Invalid ignored ->
				Optional.of(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
			case MessagesSignatureVerification.Verified ignored -> Optional.empty();
		};
	}

	private Optional<MessagesStatusRequest> parse(String rawBody) {
		if (rawBody == null || rawBody.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(jsonMapper.readValue(rawBody, MessagesStatusRequest.class));
		} catch (JacksonException exception) {
			return Optional.empty();
		}
	}

	private static boolean hasRequiredFields(MessagesStatusRequest request) {
		return isPresent(request.messageUuid()) && isPresent(request.status()) && isPresent(request.channel());
	}

	private static boolean isPresent(String value) {
		return value != null && !value.isBlank();
	}

	private static ResponseEntity<ProblemDetail> malformed() {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
			HttpStatus.BAD_REQUEST, "The status callback is malformed or misses required fields.");
		problem.setType(URI.create("about:blank"));
		return ResponseEntity.badRequest().body(problem);
	}
}
