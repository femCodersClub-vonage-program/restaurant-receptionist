package com.restaurantreceptionist.reservations;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurantreceptionist.reservations.ReservationCreationResult.Created;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InsufficientCapacity;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InvalidData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;

@RestController
@RequestMapping("/api/reservations")
public class ReservationCreationController {

	private static final String PROBLEM_JSON = "application/problem+json";

	private static final String MALFORMED = "MALFORMED";

	private final ReservationCreationService reservationCreationService;

	public ReservationCreationController(ReservationCreationService reservationCreationService) {
		this.reservationCreationService = reservationCreationService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a confirmed manual reservation")
	@ApiResponses({
		@ApiResponse(
			responseCode = "201",
			description = "Reservation confirmed and persisted",
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = ManualReservationCreatedResponse.class))),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid or malformed reservation data; includes field and reason",
			content = @Content(
				mediaType = PROBLEM_JSON,
				schema = @Schema(implementation = ProblemDetail.class))),
		@ApiResponse(
			responseCode = "409",
			description = "Insufficient capacity in the dining session",
			content = @Content(
				mediaType = PROBLEM_JSON,
				schema = @Schema(implementation = ProblemDetail.class)))
	})
	public ResponseEntity<?> createReservation(@RequestBody ManualReservationRequest request) {
		if (request.partySize() == null) {
			return invalidData(ReservationField.PARTY_SIZE, InvalidData.Reason.MISSING.name());
		}

		return switch (reservationCreationService.create(request.toCommand())) {
			case Created created -> ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ManualReservationCreatedResponse.from(created));
			case InvalidData invalid -> invalidData(invalid.field(), invalid.reason().name());
			case InsufficientCapacity insufficient -> insufficientCapacity(insufficient);
		};
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException exception) {
		return malformedField(exception.getCause())
			.map(field -> invalidData(field, MALFORMED))
			.orElseGet(() -> ResponseEntity
				.badRequest()
				.body(problem(HttpStatus.BAD_REQUEST, "The request body is not readable.")));
	}

	private static Optional<ReservationField> malformedField(Throwable cause) {
		if (!(cause instanceof DatabindException databindException)) {
			return Optional.empty();
		}
		List<JacksonException.Reference> path = databindException.getPath();
		if (path.isEmpty()) {
			return Optional.empty();
		}
		return fieldFor(path.getFirst().getPropertyName());
	}

	private static Optional<ReservationField> fieldFor(String jsonProperty) {
		return switch (jsonProperty) {
			case "customerFirstName" -> Optional.of(ReservationField.FIRST_NAME);
			case "customerLastName" -> Optional.of(ReservationField.LAST_NAME);
			case "phoneNumber" -> Optional.of(ReservationField.PHONE_NUMBER);
			case "reservationDate" -> Optional.of(ReservationField.RESERVATION_DATE);
			case "arrivalTime" -> Optional.of(ReservationField.ARRIVAL_TIME);
			case "partySize" -> Optional.of(ReservationField.PARTY_SIZE);
			case null, default -> Optional.empty();
		};
	}

	private static ResponseEntity<ProblemDetail> invalidData(ReservationField field, String reason) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "The reservation request contains invalid data.");
		problem.setProperty("field", field.name());
		problem.setProperty("reason", reason);
		return ResponseEntity.badRequest().body(problem);
	}

	private static ResponseEntity<ProblemDetail> insufficientCapacity(InsufficientCapacity insufficient) {
		ProblemDetail problem = problem(
			HttpStatus.CONFLICT,
			"The dining session does not have enough available capacity for the requested party size.");
		problem.setProperty("diningSession", insufficient.capacity().diningSession().name());
		problem.setProperty("availableCapacity", insufficient.capacity().availableCapacity());
		problem.setProperty("requestedPartySize", insufficient.requestedPartySize());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
	}

	private static ProblemDetail problem(HttpStatus status, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(URI.create("about:blank"));
		return problem;
	}
}
