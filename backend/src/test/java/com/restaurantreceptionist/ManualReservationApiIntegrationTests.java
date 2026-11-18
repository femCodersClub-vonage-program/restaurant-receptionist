package com.restaurantreceptionist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.restaurantreceptionist.reservations.DiningSession;
import com.restaurantreceptionist.reservations.Reservation;
import com.restaurantreceptionist.reservations.ReservationRepository;
import com.restaurantreceptionist.reservations.ReservationStatus;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ManualReservationApiIntegrationTests {

	private static final String FICTIONAL_PHONE_NUMBER = "+34600000000";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private Clock clock;

	private LocalDate reservationDate;

	@BeforeEach
	void setUp() {
		reservationDate = LocalDate.now(clock).plusDays(30);
		deleteReservationsOnTestDate();
	}

	@AfterEach
	void tearDown() {
		deleteReservationsOnTestDate();
	}

	@Test
	void persistsConfirmedReservationWithNormalizedPhoneNumber() throws Exception {
		ResultActions response = postReservation(requestBody("600 000 000", reservationDate, "4"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.diningSession").value("DINNER"));

		assertThat(dinnerReservations()).hasSize(1);
		Reservation reservation = dinnerReservations().getFirst();
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
		assertThat(reservation.getPhoneNumber()).isEqualTo(FICTIONAL_PHONE_NUMBER);
		assertThat(reservation.getPartySize()).isEqualTo((short) 4);

		response.andExpect(jsonPath("$.id").value(reservation.getId().toString()));
	}

	@Test
	void rejectsNonSpanishPhoneNumberWithoutPersisting() throws Exception {
		postReservation(requestBody("+351912345678", reservationDate, "4"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.field").value("PHONE_NUMBER"))
			.andExpect(jsonPath("$.reason").value("INVALID_FORMAT"));

		assertThat(dinnerReservations()).isEmpty();
	}

	@Test
	void rejectsPastDateWithoutPersisting() throws Exception {
		LocalDate yesterday = LocalDate.now(clock).minusDays(1);

		postReservation(requestBody(FICTIONAL_PHONE_NUMBER, yesterday, "4"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.field").value("RESERVATION_DATE"))
			.andExpect(jsonPath("$.reason").value("PAST_DATE"));

		assertThat(reservationRepository.findAllByReservationDateAndDiningSessionOrderByArrivalTimeAsc(
			yesterday, DiningSession.DINNER)).isEmpty();
	}

	@Test
	void rejectsMalformedPartySizeWithoutPersisting() throws Exception {
		postReservation(requestBody(FICTIONAL_PHONE_NUMBER, reservationDate, "\"4\""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.field").value("PARTY_SIZE"))
			.andExpect(jsonPath("$.reason").value("MALFORMED"));

		assertThat(dinnerReservations()).isEmpty();
	}

	@Test
	void returnsConflictWithoutPersistingWhenCapacityIsInsufficient() throws Exception {

		prefillDinner(28);

		postReservation(requestBody(FICTIONAL_PHONE_NUMBER, reservationDate, "4"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.diningSession").value("DINNER"))
			.andExpect(jsonPath("$.availableCapacity").value(2))
			.andExpect(jsonPath("$.requestedPartySize").value(4));

		assertThat(confirmedDinnerGuests()).isEqualTo(28);
	}

	private ResultActions postReservation(String body) throws Exception {
		return mockMvc.perform(post("/api/reservations")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}

	private static String requestBody(String phoneNumber, LocalDate date, String partySizeJson) {
		return """
			{
				"customerFirstName": "Fictional",
				"customerLastName": "Guest",
				"phoneNumber": "%s",
				"reservationDate": "%s",
				"arrivalTime": "21:00",
				"partySize": %s
			}
			""".formatted(phoneNumber, date, partySizeJson);
	}

	private List<Reservation> dinnerReservations() {
		return reservationRepository.findAllByReservationDateAndDiningSessionOrderByArrivalTimeAsc(reservationDate, DiningSession.DINNER);
	}

	private void prefillDinner(int guests) {
		reservationRepository.saveAndFlush(new Reservation(
			"Fictional",
			"Guest",
			FICTIONAL_PHONE_NUMBER,
			reservationDate,
			LocalTime.of(20, 0),
			DiningSession.DINNER,
			(short) guests));
	}

	private long confirmedDinnerGuests() {
		return reservationRepository.sumConfirmedPartySize(reservationDate, DiningSession.DINNER);
	}

	private void deleteReservationsOnTestDate() {
		jdbcTemplate.update("DELETE FROM reservations WHERE reservation_date = ?", reservationDate);
	}
}
