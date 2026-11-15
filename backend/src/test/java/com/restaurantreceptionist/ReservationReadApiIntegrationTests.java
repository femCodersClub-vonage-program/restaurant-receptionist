package com.restaurantreceptionist;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantreceptionist.reservations.DiningSession;
import com.restaurantreceptionist.reservations.Reservation;
import com.restaurantreceptionist.reservations.ReservationRepository;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationReadApiIntegrationTests {

	private static final LocalDate DATE = LocalDate.of(2027, 5, 14);
	private static final LocalDate HISTORICAL_DATE = LocalDate.of(2020, 1, 2);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ReservationRepository reservationRepository;

	@Test
	void returnsBothEmptySessionsAndTheirCapacity() throws Exception {
		mockMvc.perform(get("/api/reservations").param("date", DATE.toString()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.date").value(DATE.toString()))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations").isEmpty())
				.andExpect(jsonPath("$.sessions.LUNCH.capacity.totalCapacity").value(30))
				.andExpect(jsonPath("$.sessions.LUNCH.capacity.occupiedCapacity").value(0))
				.andExpect(jsonPath("$.sessions.LUNCH.capacity.availableCapacity").value(30))
				.andExpect(jsonPath("$.sessions.DINNER.reservations").isEmpty())
				.andExpect(jsonPath("$.sessions.DINNER.capacity.totalCapacity").value(30))
				.andExpect(jsonPath("$.sessions.DINNER.capacity.occupiedCapacity").value(0))
				.andExpect(jsonPath("$.sessions.DINNER.capacity.availableCapacity").value(30));
	}

	@Test
	void returnsPastDateReservationsOrderedByArrivalWithCapacityForEachSession() throws Exception {
		var lateLunch = reservation(HISTORICAL_DATE, LocalTime.of(15, 45), DiningSession.LUNCH, (short) 3);
		var dinner = reservation(HISTORICAL_DATE, LocalTime.of(20, 30), DiningSession.DINNER, (short) 4);
		var earlyLunch = reservation(HISTORICAL_DATE, LocalTime.of(13, 15), DiningSession.LUNCH, (short) 2);
		var otherDate = reservation(HISTORICAL_DATE.plusDays(1), LocalTime.of(13, 0), DiningSession.LUNCH, (short) 10);
		reservationRepository.saveAllAndFlush(List.of(lateLunch, dinner, earlyLunch, otherDate));

		mockMvc.perform(get("/api/reservations").param("date", HISTORICAL_DATE.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.date").value(HISTORICAL_DATE.toString()))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations.length()").value(2))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[0].id").value(earlyLunch.getId().toString()))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[0].customerFirstName").value("Fictional"))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[0].customerLastName").value("Guest"))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[0].phoneNumber").value("fictional-test-contact"))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[0].reservationDate")
						.value(HISTORICAL_DATE.toString()))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[0].arrivalTime").value("13:15"))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[1].id").value(lateLunch.getId().toString()))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[1].arrivalTime").value("15:45"))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[0].partySize").value(2))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[0].status").value("CONFIRMED"))
				.andExpect(jsonPath("$.sessions.LUNCH.reservations[0].createdAt").doesNotExist())
				.andExpect(jsonPath("$.sessions.LUNCH.capacity.totalCapacity").value(30))
				.andExpect(jsonPath("$.sessions.LUNCH.capacity.occupiedCapacity").value(5))
				.andExpect(jsonPath("$.sessions.LUNCH.capacity.availableCapacity").value(25))
				.andExpect(jsonPath("$.sessions.DINNER.reservations.length()").value(1))
				.andExpect(jsonPath("$.sessions.DINNER.reservations[0].id").value(dinner.getId().toString()))
				.andExpect(jsonPath("$.sessions.DINNER.capacity.totalCapacity").value(30))
				.andExpect(jsonPath("$.sessions.DINNER.capacity.occupiedCapacity").value(4))
				.andExpect(jsonPath("$.sessions.DINNER.capacity.availableCapacity").value(26));
	}

	@Test
	void missingDateReturnsProblemDetails() throws Exception {
		mockMvc.perform(get("/api/reservations"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/problem+json")))
				.andExpect(jsonPath("$.type").value("about:blank"))
				.andExpect(jsonPath("$.title").value("Bad Request"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("Query parameter 'date' is required."));
	}

	@Test
	void invalidDateReturnsProblemDetails() throws Exception {
		mockMvc.perform(get("/api/reservations").param("date", "not-a-date"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/problem+json")))
				.andExpect(jsonPath("$.type").value("about:blank"))
				.andExpect(jsonPath("$.title").value("Bad Request"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail")
						.value("Query parameter 'date' must be a valid calendar date in YYYY-MM-DD format."));
	}

	private Reservation reservation(
			LocalDate date, LocalTime time, DiningSession diningSession, short partySize) {
		return new Reservation(
				"Fictional",
				"Guest",
				"fictional-test-contact",
				date,
				time,
				diningSession,
				partySize);
	}
}
