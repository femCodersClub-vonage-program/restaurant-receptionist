package com.restaurantreceptionist.reservations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.restaurantreceptionist.availability.CapacitySummary;
import com.restaurantreceptionist.reservations.ReservationCreationResult.Created;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InsufficientCapacity;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InvalidData;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InvalidData.Reason;

@WebMvcTest(ReservationCreationController.class)
class ReservationCreationControllerTests {

	private static final MediaType PROBLEM_JSON = MediaType.valueOf("application/problem+json");

	private static final UUID RESERVATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	private static final Map<String, String> VALID_VALUES = Map.of(
		"customerFirstName", "\"Fictional\"",
		"customerLastName", "\"Guest\"",
		"phoneNumber", "\"fictional-test-contact\"",
		"reservationDate", "\"2027-05-14\"",
		"arrivalTime", "\"21:00\"",
		"partySize", "4");

	private static final ConfirmedReservationCommand EXPECTED_COMMAND = new ConfirmedReservationCommand(
		"Fictional",
		"Guest",
		"fictional-test-contact",
		LocalDate.of(2027, 5, 14),
		LocalTime.of(21, 0),
		4);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReservationCreationService reservationCreationService;

	@Test
	void createdReservationReturns201WithIdAndDiningSession() throws Exception {
		when(reservationCreationService.create(any()))
			.thenReturn(new Created(RESERVATION_ID, DiningSession.DINNER));

		postReservation(json(VALID_VALUES))
			.andExpect(status().isCreated())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").value(RESERVATION_ID.toString()))
			.andExpect(jsonPath("$.diningSession").value("DINNER"))
			.andExpect(header().doesNotExist("Location"));
	}

	@Test
	void clientSuppliedDiningSessionIsIgnored() throws Exception {
		when(reservationCreationService.create(any()))
			.thenReturn(new Created(RESERVATION_ID, DiningSession.DINNER));

		postReservation(json(validValuesWith("diningSession", "\"LUNCH\"")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.diningSession").value("DINNER"));

		verify(reservationCreationService).create(EXPECTED_COMMAND);
	}

	@Test
	void invalidDataFromServiceReturns400WithFieldAndReason() throws Exception {
		when(reservationCreationService.create(any()))
			.thenReturn(new InvalidData(ReservationField.RESERVATION_DATE, ReservationCreationResult.InvalidData.Reason.PAST_DATE));

		postReservation(json(VALID_VALUES))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Bad Request"))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.detail").value("The reservation request contains invalid data."))
			.andExpect(jsonPath("$.field").value("RESERVATION_DATE"))
			.andExpect(jsonPath("$.reason").value("PAST_DATE"));
	}

	@Test
	void insufficientCapacityReturns409WithCapacityExtensions() throws Exception {
		when(reservationCreationService.create(any()))
			.thenReturn(new InsufficientCapacity(new CapacitySummary(DiningSession.DINNER, 30, 27, 3), 4));

		postReservation(json(VALID_VALUES))
			.andExpect(status().isConflict())
			.andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Conflict"))
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.detail").value("The dining session does not have enough available capacity for the requested party size."))
			.andExpect(jsonPath("$.diningSession").value("DINNER"))
			.andExpect(jsonPath("$.availableCapacity").value(3))
			.andExpect(jsonPath("$.requestedPartySize").value(4));
	}

	private ResultActions postReservation(String body) throws Exception {
		return mockMvc.perform(post("/api/reservations")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}

	private static Map<String, String> validValuesWith(String property, String rawJsonValue) {
		Map<String, String> values = new HashMap<>(VALID_VALUES);
		values.put(property, rawJsonValue);
		return values;
	}

	private static String json(Map<String, String> rawJsonValues) {
		return rawJsonValues.entrySet().stream()
			.map(entry -> "\"" + entry.getKey() + "\" : " + entry.getValue())
			.collect(Collectors.joining(", ", "{", "}"));
	}

}
