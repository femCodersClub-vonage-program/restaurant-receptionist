package com.restaurantreceptionist;

import com.restaurantreceptionist.availability.CapacitySummary;
import com.restaurantreceptionist.reservations.*;
import com.restaurantreceptionist.reservations.ReservationCreationResult.Created;
import com.restaurantreceptionist.reservations.ReservationCreationResult.InsufficientCapacity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ReservationCreationServiceIntegrationTests {

	private static final LocalTime LUNCH_TIME = LocalTime.of(14, 0);

	@Autowired
	private ReservationCreationService reservationCreationService;

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
	void createsConfirmedReservationWhenCapacityIsAvailable() {
		ReservationCreationResult result = reservationCreationService.create(command(LUNCH_TIME, 2));

		assertThat(result).isInstanceOf(Created.class);
		Created created = (Created) result;

		assertThat(created.diningSession()).isEqualTo(DiningSession.LUNCH);

		assertThat(reservationRepository.findAllByReservationDateAndDiningSessionOrderByArrivalTimeAsc(
			reservationDate, DiningSession.LUNCH))
			.singleElement()
			.satisfies(reservation -> {
				assertThat(reservation.getId()).isEqualTo(created.reservationId());
				assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
				assertThat(reservation.getPartySize()).isEqualTo((short) 2);
			});
		assertThat(confirmedLunchGuests()).isEqualTo(2);
	}

	@Test
	void rejectsReservationWhenPartyDoesNotFitRemainingCapacity() {
		prefillLunch(28);

		ReservationCreationResult result = reservationCreationService.create(command(LUNCH_TIME, 3));

		assertThat(result).isEqualTo(new InsufficientCapacity(
			new CapacitySummary(DiningSession.LUNCH, 30, 28, 2), 3));
		assertThat(confirmedLunchGuests()).isEqualTo(28);
	}

	@Test
	void lockCapacityRequiresAnActiveTransaction() {
		assertThatThrownBy(() -> reservationRepository.lockCapacity(reservationDate, DiningSession.LUNCH))
			.isInstanceOf(IllegalTransactionStateException.class);
	}

	@Test
	void concurrentRequestsNeverExceedServiceCapacity() throws Exception {
		prefillLunch(28);
		int concurrentRequests = 8;
		ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
		CountDownLatch ready = new CountDownLatch(concurrentRequests);
		CountDownLatch start = new CountDownLatch(1);

		try {
			List<Future<ReservationCreationResult>> futures = new ArrayList<>();
			for (int i = 0; i < concurrentRequests; i++) {
				LocalTime arrivalTime = LUNCH_TIME.plusMinutes(5L * i);
				futures.add(executor.submit(() -> {
					ready.countDown();
					start.await();
					return reservationCreationService.create(command(arrivalTime, 2));
				}));
			}

			assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
			start.countDown();

			List<ReservationCreationResult> results = new ArrayList<>();
			for (Future<ReservationCreationResult> future : futures) {
				results.add(future.get(30, TimeUnit.SECONDS));
			}

			assertThat(results).filteredOn(Created.class::isInstance).hasSize(1);
			assertThat(results).filteredOn(InsufficientCapacity.class::isInstance).hasSize(concurrentRequests - 1);
			assertThat(confirmedLunchGuests()).isEqualTo(30);
		} finally {
			executor.shutdownNow();
		}
	}

	private void deleteReservationsOnTestDate() {
		jdbcTemplate.update("DELETE FROM reservations WHERE reservation_date = ?", reservationDate);
	}

	private ConfirmedReservationCommand command(LocalTime arrivalTime, int partySize) {
		return new ConfirmedReservationCommand(
			"Fictional",
			"Guest",
			"fictional-test-contact",
			reservationDate,
			arrivalTime,
			partySize);
	}

	private void prefillLunch(int guests) {
		reservationRepository.saveAndFlush(new Reservation(
			"Fictional",
			"Guest",
			"fictional-test-contact",
			reservationDate,
			LocalTime.of(13, 0),
			DiningSession.LUNCH,
			(short) guests));
	}

	private long confirmedLunchGuests() {
		return reservationRepository.sumConfirmedPartySize(reservationDate, DiningSession.LUNCH);
	}
}
