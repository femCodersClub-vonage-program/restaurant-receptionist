package com.restaurantreceptionist.voice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurantreceptionist.config.ClockConfiguration;
import com.restaurantreceptionist.config.VoiceProperties;

@ExtendWith(MockitoExtension.class)
class VoiceCallSessionServiceTests {

	private static final Instant NOW = Instant.parse("2027-05-14T13:30:00Z");

	private static final Clock CLOCK = Clock.fixed(NOW, ClockConfiguration.RESTAURANT_ZONE);

	private static final Duration TIMEOUT = Duration.ofMinutes(20);

	@Mock
	private VoiceCallSessionRepository repository;

	private VoiceCallSessionService service;

	@BeforeEach
	void setUp() {
		service = new VoiceCallSessionService(repository, CLOCK, new VoiceProperties(TIMEOUT), null);
	}

	@Test
	void expireStaleSessionsMarksStaleActiveSessionsExpired() {
		var stale = VoiceCallSession.create("stale-uuid", null);
		when(repository.findAllByStateAndUpdatedAtBefore(eq(CallState.ACTIVE), any(Instant.class)))
			.thenReturn(List.of(stale));

		int expired = service.expireStaleSessions();

		assertThat(expired).isEqualTo(1);
		assertThat(stale.getState()).isEqualTo(CallState.EXPIRED);
		assertThat(stale.getTerminalAt()).isEqualTo(NOW);

		ArgumentCaptor<Instant> cutoff = ArgumentCaptor.forClass(Instant.class);
		verify(repository).findAllByStateAndUpdatedAtBefore(eq(CallState.ACTIVE), cutoff.capture());
		assertThat(cutoff.getValue()).isEqualTo(NOW.minus(TIMEOUT));
	}

	@Test
	void expireStaleSessionsReturnsZeroWhenNothingIsStale() {
		when(repository.findAllByStateAndUpdatedAtBefore(eq(CallState.ACTIVE), any(Instant.class)))
			.thenReturn(List.of());

		assertThat(service.expireStaleSessions()).isZero();
	}

	@Test
	void errorWebhookIsAcknowledgedWithoutStateChange() {
		var result = service.handleEvent(new CallEvent(null, "conversation-uuid", null, null, "ncco error", null, NOW));

		assertThat(result).isEqualTo(new CallEventResult.Acknowledged());
		verifyNoInteractions(repository);
	}

	@Test
	void terminalEventUsesEventTimestampForTerminalAt() {
		var session = VoiceCallSession.create("call-leg-uuid", null);
		when(repository.findByUuid("call-leg-uuid")).thenReturn(Optional.of(session));

		var eventTime = Instant.parse("2027-05-14T13:00:00Z");
		service.handleEvent(new CallEvent("call-leg-uuid", null, "completed", "user", "caller hung up", null, eventTime));

		assertThat(session.getState()).isEqualTo(CallState.ABANDONED);
		assertThat(session.getTerminalAt()).isEqualTo(eventTime);
	}

	@Test
	void rejectedEventUsesDetailAsEndReason() {
		var session = VoiceCallSession.create("call-leg-uuid", null);
		when(repository.findByUuid("call-leg-uuid")).thenReturn(Optional.of(session));

		service.handleEvent(new CallEvent("call-leg-uuid", null, "rejected", null, null, "declined", null));

		assertThat(session.getState()).isEqualTo(CallState.ABANDONED);
		assertThat(session.getEndReason()).isEqualTo("declined");
	}

	@Test
	void errorWebhookWithoutUuidIsAcknowledgedWithoutStateChange() {
		var result = service.handleEvent(new CallEvent(null, null, null, null, "ncco error", null, null));

		assertThat(result).isEqualTo(new CallEventResult.Acknowledged());
		verifyNoInteractions(repository);
	}

	@Test
	void answerReturnsOpenedWhenSessionIsActive() {
		when(repository.findByUuid("call-leg-uuid")).thenReturn(Optional.of(VoiceCallSession.create("call-leg-uuid", null)));

		var result = service.answer("call-leg-uuid", null);

		assertThat(result).isInstanceOf(AnswerResult.Opened.class);
		verify(repository).insertIfAbsent("call-leg-uuid", null, "ACTIVE", "INITIAL_GREETING");
	}

	@Test
	void answerReturnsAlreadyTerminalWhenSessionIsTerminal() {
		when(repository.findByUuid("call-leg-uuid"))
			.thenReturn(Optional.of(VoiceCallSession.createAsAbandoned("call-leg-uuid", null, null, null, NOW)));

		var result = service.answer("call-leg-uuid", null);

		assertThat(result).isInstanceOf(AnswerResult.AlreadyTerminal.class);
	}

	@Test
	void nonTerminalEventOnExistingSessionIsAcknowledgedWithoutStateChange() {
		var session = VoiceCallSession.create("call-leg-uuid", null);
		when(repository.findByUuid("call-leg-uuid")).thenReturn(Optional.of(session));

		var result = service.handleEvent(new CallEvent("call-leg-uuid", null, "ringing", null, null, null, NOW));

		assertThat(result).isEqualTo(new CallEventResult.Acknowledged());
		assertThat(session.getState()).isEqualTo(CallState.ACTIVE);
	}

	@Test
	void terminalEventOnUnknownSessionPersistsTombstone() {
		when(repository.findByUuid("call-leg-uuid")).thenReturn(Optional.empty());
		when(repository.insertTerminalIfAbsent("call-leg-uuid", null, null, "CALLER", NOW)).thenReturn(1);

		var result = service.handleEvent(new CallEvent("call-leg-uuid", null, "completed", "user", null, null, NOW));

		assertThat(result).isInstanceOf(CallEventResult.Terminated.class);
		verify(repository).insertTerminalIfAbsent("call-leg-uuid", null, null, "CALLER", NOW);
	}

	@Test
	void terminalEventOnUnknownSessionWhenAnswerWonMarksSessionAbandoned() {
		var session = VoiceCallSession.create("call-leg-uuid", null);
		when(repository.findByUuid("call-leg-uuid")).thenReturn(Optional.empty()).thenReturn(Optional.of(session));
		when(repository.insertTerminalIfAbsent("call-leg-uuid", null, null, "CALLER", NOW)).thenReturn(0);

		var result = service.handleEvent(new CallEvent("call-leg-uuid", null, "completed", "user", null, null, NOW));

		assertThat(session.getState()).isEqualTo(CallState.ABANDONED);
		assertThat(result).isInstanceOf(CallEventResult.Terminated.class);
	}

	@Test
	void terminalEventOnAlreadyTerminalSessionDoesNotRegress() {
		var session = VoiceCallSession.create("call-leg-uuid", null);
		session.markExpired(NOW);
		when(repository.findByUuid("call-leg-uuid")).thenReturn(Optional.of(session));

		var result = service.handleEvent(new CallEvent("call-leg-uuid", null, "completed", "user", null, null, NOW));

		assertThat(result).isEqualTo(new CallEventResult.Acknowledged());
		assertThat(session.getState()).isEqualTo(CallState.EXPIRED);
	}
}
