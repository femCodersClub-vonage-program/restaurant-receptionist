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

	@Test
	void expireStaleSessionsMarksStaleActiveSessionsExpired() {
		var service = new VoiceCallSessionService(repository, CLOCK, new VoiceProperties(TIMEOUT), null);
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
		var service = new VoiceCallSessionService(repository, CLOCK, new VoiceProperties(TIMEOUT), null);
		when(repository.findAllByStateAndUpdatedAtBefore(eq(CallState.ACTIVE), any(Instant.class)))
			.thenReturn(List.of());

		assertThat(service.expireStaleSessions()).isZero();
	}

	@Test
	void errorWebhookMarksExistingActiveSessionAbandoned() {
		var service = new VoiceCallSessionService(repository, CLOCK, new VoiceProperties(TIMEOUT), null);
		var session = VoiceCallSession.create("call-leg-uuid", null);
		when(repository.findByUuid("call-leg-uuid")).thenReturn(Optional.of(session));

		var result = service.handleEvent(new CallEvent("call-leg-uuid", null, null, null, "ncco error"));

		assertThat(result).isInstanceOfSatisfying(CallEventResult.Terminated.class,
			terminated -> assertThat(terminated.state()).isEqualTo(CallState.ABANDONED));
		assertThat(session.getState()).isEqualTo(CallState.ABANDONED);
		assertThat(session.getEndReason()).isEqualTo("ncco error");
		assertThat(session.getTerminalAt()).isEqualTo(NOW);
	}

	@Test
	void errorWebhookWithoutUuidIsAcknowledgedWithoutStateChange() {
		var service = new VoiceCallSessionService(repository, CLOCK, new VoiceProperties(TIMEOUT), null);

		var result = service.handleEvent(new CallEvent(null, null, null, null, "ncco error"));

		assertThat(result).isEqualTo(new CallEventResult.Acknowledged());
		verifyNoInteractions(repository);
	}
}
