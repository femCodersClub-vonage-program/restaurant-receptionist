package com.restaurantreceptionist.voice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.restaurantreceptionist.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class VoiceCallSessionOrderingIntegrationTests {

	@Autowired
	private VoiceCallSessionService voiceCallSessionService;

	@Autowired
	private VoiceCallSessionRepository voiceCallSessionRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void completedBeforeAnswerPersistsTombstoneAndDoesNotOpenActiveSession() {
		var uuid = UUID.randomUUID().toString();

		var eventResult = voiceCallSessionService.handleEvent(completed(uuid));
		var answerResult = voiceCallSessionService.answer(uuid, null);

		assertThat(eventResult).isInstanceOf(CallEventResult.Terminated.class);
		assertThat(answerResult).isInstanceOf(AnswerResult.AlreadyTerminal.class);
		assertThat(voiceCallSessionRepository.findByUuid(uuid).orElseThrow().getState())
			.isEqualTo(CallState.ABANDONED);
	}

	@Test
	void answerBeforeCompletedMarksSessionAbandoned() {
		var uuid = UUID.randomUUID().toString();

		var answerResult = voiceCallSessionService.answer(uuid, null);
		var eventResult = voiceCallSessionService.handleEvent(completed(uuid));

		assertThat(answerResult).isInstanceOf(AnswerResult.Opened.class);
		assertThat(eventResult).isInstanceOf(CallEventResult.Terminated.class);
		assertThat(voiceCallSessionRepository.findByUuid(uuid).orElseThrow().getState())
			.isEqualTo(CallState.ABANDONED);
	}

	@Test
	void answerAndCompletedArrivingTogetherLeaveTerminalSession() throws Exception {
		var uuid = UUID.randomUUID().toString();
		var start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		try {
			Future<AnswerResult> answerFuture = executor.submit(() -> {
				await(start);
				return voiceCallSessionService.answer(uuid, null);
			});
			Future<CallEventResult> eventFuture = executor.submit(() -> {
				await(start);
				return voiceCallSessionService.handleEvent(completed(uuid));
			});

			start.countDown();
			answerFuture.get(10, TimeUnit.SECONDS);
			eventFuture.get(10, TimeUnit.SECONDS);
		} finally {
			executor.shutdownNow();
		}

		assertThat(voiceCallSessionRepository.findByUuid(uuid).orElseThrow().getState())
			.isEqualTo(CallState.ABANDONED);
	}

	@Test
	void twoSimultaneousAnswersCreateOnlyOneSession() throws Exception {
		var uuid = UUID.randomUUID().toString();
		var start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		try {
			Future<AnswerResult> first = executor.submit(() -> {
				await(start);
				return voiceCallSessionService.answer(uuid, null);
			});
			Future<AnswerResult> second = executor.submit(() -> {
				await(start);
				return voiceCallSessionService.answer(uuid, null);
			});

			start.countDown();
			assertThat(first.get(10, TimeUnit.SECONDS)).isInstanceOf(AnswerResult.Opened.class);
			assertThat(second.get(10, TimeUnit.SECONDS)).isInstanceOf(AnswerResult.Opened.class);
		} finally {
			executor.shutdownNow();
		}

		Integer count = jdbcTemplate.queryForObject(
			"SELECT count(*) FROM voice_call_sessions WHERE uuid = ?", Integer.class, uuid);
		assertThat(count).isEqualTo(1);
		assertThat(voiceCallSessionRepository.findByUuid(uuid).orElseThrow().getState())
			.isEqualTo(CallState.ACTIVE);
	}

	@Test
	void twoCompletedEventsTerminateSameSessionExactlyOnce() throws Exception {
		var uuid = UUID.randomUUID().toString();
		voiceCallSessionService.answer(uuid, null);

		var start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		try {
			Future<CallEventResult> first = executor.submit(() -> {
				await(start);
				return voiceCallSessionService.handleEvent(completed(uuid));
			});
			Future<CallEventResult> second = executor.submit(() -> {
				await(start);
				return voiceCallSessionService.handleEvent(completed(uuid));
			});

			start.countDown();
			CallEventResult firstResult = first.get(10, TimeUnit.SECONDS);
			CallEventResult secondResult = second.get(10, TimeUnit.SECONDS);

			assertThat(List.of(isTerminated(firstResult), isTerminated(secondResult)))
				.containsExactlyInAnyOrder(true, false);
		} finally {
			executor.shutdownNow();
		}

		var stored = voiceCallSessionRepository.findByUuid(uuid).orElseThrow();
		assertThat(stored.getState()).isEqualTo(CallState.ABANDONED);
		assertThat(stored.getVersion()).isEqualTo(1L);
	}

	private static boolean isTerminated(CallEventResult result) {
		return result instanceof CallEventResult.Terminated;
	}

	private static CallEvent completed(String uuid) {
		return new CallEvent(uuid, null, "completed", "user", null, null, null);
	}

	private static void await(CountDownLatch latch) {
		try {
			if (!latch.await(10, TimeUnit.SECONDS)) {
				throw new IllegalStateException("Timed out waiting for the other thread");
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(exception);
		}
	}
}
