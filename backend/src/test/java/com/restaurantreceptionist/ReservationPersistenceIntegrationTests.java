package com.restaurantreceptionist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantreceptionist.reservations.Reservation;
import com.restaurantreceptionist.reservations.ReservationRepository;
import com.restaurantreceptionist.reservations.ReservationService;
import com.restaurantreceptionist.reservations.ReservationStatus;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class ReservationPersistenceIntegrationTests {

	@Autowired
	private Flyway flyway;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ReservationRepository reservationRepository;

	@Test
	void appliesReservationsMigration() {
		assertThat(flyway.info().applied())
				.extracting(migration -> migration.getVersion().getVersion())
				.contains("1");
		assertThat(jdbcTemplate.queryForObject(
				"SELECT to_regclass('public.reservations') IS NOT NULL", Boolean.class)).isTrue();
	}

	@Test
	void persistsAndRetrievesConfirmedReservation() {
		var reservation = new Reservation(
				"Fictional",
				"Guest",
				"fictional-test-contact",
				LocalDate.of(2027, 5, 14),
				LocalTime.of(13, 30),
				ReservationService.LUNCH,
				(short) 4);

		var savedReservation = reservationRepository.saveAndFlush(reservation);
		var retrievedReservation = reservationRepository.findById(savedReservation.getId()).orElseThrow();

		assertThat(retrievedReservation.getCustomerFirstName()).isEqualTo("Fictional");
		assertThat(retrievedReservation.getCustomerLastName()).isEqualTo("Guest");
		assertThat(retrievedReservation.getPhoneNumber()).isEqualTo("fictional-test-contact");
		assertThat(retrievedReservation.getReservationDate()).isEqualTo(LocalDate.of(2027, 5, 14));
		assertThat(retrievedReservation.getArrivalTime()).isEqualTo(LocalTime.of(13, 30));
		assertThat(retrievedReservation.getService()).isEqualTo(ReservationService.LUNCH);
		assertThat(retrievedReservation.getPartySize()).isEqualTo((short) 4);
		assertThat(retrievedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
		assertThat(retrievedReservation.getCreatedAt()).isNotNull();
	}

	@Test
	void findsReservationsForDateAndServiceOrderedByArrivalTime() {
		var date = LocalDate.of(2027, 5, 14);
		var earlyLunch = reservation(date, LocalTime.of(13, 15), ReservationService.LUNCH, (short) 2);
		var lateLunch = reservation(date, LocalTime.of(15, 45), ReservationService.LUNCH, (short) 3);
		var dinner = reservation(date, LocalTime.of(20, 30), ReservationService.DINNER, (short) 2);
		var otherDate = reservation(date.plusDays(1), LocalTime.of(13, 0), ReservationService.LUNCH, (short) 2);

		reservationRepository.saveAllAndFlush(List.of(lateLunch, dinner, otherDate, earlyLunch));

		assertThat(reservationRepository.findAllByReservationDateAndServiceOrderByArrivalTimeAsc(
				date, ReservationService.LUNCH))
				.extracting(Reservation::getId)
				.containsExactly(earlyLunch.getId(), lateLunch.getId());
	}

	@Test
	void rejectsPartySizeBelowOne() {
		var reservation = reservation(
				LocalDate.of(2027, 5, 14),
				LocalTime.of(13, 30),
				ReservationService.LUNCH,
				(short) 0);

		assertThatThrownBy(() -> reservationRepository.saveAndFlush(reservation))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsPartySizeAboveThirty() {
		var reservation = reservation(
				LocalDate.of(2027, 5, 14),
				LocalTime.of(13, 30),
				ReservationService.LUNCH,
				(short) 31);

		assertThatThrownBy(() -> reservationRepository.saveAndFlush(reservation))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsUnsupportedService() {
		assertThatThrownBy(() -> jdbcTemplate.update("""
				INSERT INTO reservations (
					customer_first_name, customer_last_name, phone_number,
					reservation_date, arrival_time, service, party_size
				) VALUES (
					'Fictional', 'Guest', 'fictional-test-contact',
					'2027-05-14', '13:30', 'BRUNCH', 2
				)
				"""))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Reservation reservation(
			LocalDate date, LocalTime time, ReservationService service, short partySize) {
		return new Reservation(
				"Fictional",
				"Guest",
				"fictional-test-contact",
				date,
				time,
				service,
				partySize);
	}
}
