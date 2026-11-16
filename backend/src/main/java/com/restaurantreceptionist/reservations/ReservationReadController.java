package com.restaurantreceptionist.reservations;

import java.net.URI;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/reservations")
public class ReservationReadController {

	private final ReservationReadService reservationReadService;

	public ReservationReadController(ReservationReadService reservationReadService) {
		this.reservationReadService = reservationReadService;
	}

	@GetMapping
	@Operation(summary = "Read reservations and capacity for a date")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Reservations and capacity grouped by dining session",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ReservationReadResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "The date query parameter is missing or invalid",
					content = @Content(
							mediaType = "application/problem+json",
							schema = @Schema(implementation = ProblemDetail.class)))
	})
	public ReservationReadResponse getReservations(
			@Parameter(description = "Date to read in YYYY-MM-DD format", example = "2027-05-14")
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return reservationReadService.findByDate(date);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	ResponseEntity<ProblemDetail> handleMissingDate(MissingServletRequestParameterException exception) {
		return badRequest("Query parameter 'date' is required.");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ProblemDetail> handleInvalidDate(MethodArgumentTypeMismatchException exception) {
		return badRequest("Query parameter 'date' must be a valid calendar date in YYYY-MM-DD format.");
	}

	private ResponseEntity<ProblemDetail> badRequest(String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				detail);
		problem.setType(URI.create("about:blank"));
		return ResponseEntity.badRequest().body(problem);
	}
}
