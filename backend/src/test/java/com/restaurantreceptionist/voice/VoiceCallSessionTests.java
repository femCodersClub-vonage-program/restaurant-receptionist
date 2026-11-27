package com.restaurantreceptionist.voice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class VoiceCallSessionTests {

	private static final Instant NOW = Instant.parse("2027-05-14T13:30:00Z");

	@Test
	void markAbandonedSetsTerminalStateWhenActive() {
		var session = newSession();

		boolean applied = session.markAbandoned(EndReason.DECLINED, DisconnectedBy.CALLER, NOW);

		assertThat(applied).isTrue();
		assertThat(session.getState()).isEqualTo(CallState.ABANDONED);
		assertThat(session.getEndReason()).isEqualTo(EndReason.DECLINED);
		assertThat(session.getDisconnectedBy()).isEqualTo(DisconnectedBy.CALLER);
		assertThat(session.getTerminalAt()).isEqualTo(NOW);
	}

	@Test
	void markAbandonedDoesNotRegressTerminalState() {
		var session = newSession();
		session.markExpired(NOW);

		boolean applied = session.markAbandoned(EndReason.DECLINED, DisconnectedBy.CALLER, NOW);

		assertThat(applied).isFalse();
		assertThat(session.getState()).isEqualTo(CallState.EXPIRED);
		assertThat(session.getEndReason()).isNull();
		assertThat(session.getDisconnectedBy()).isNull();
		assertThat(session.getTerminalAt()).isEqualTo(NOW);
	}

	@Test
	void markAbandonedRejectsNullNow() {
		assertThatNullPointerException()
				.isThrownBy(() -> newSession().markAbandoned(EndReason.DECLINED, DisconnectedBy.CALLER, null));
	}

	@Test
	void markExpiredSetsExpiredStateWhenActive() {
		var session = newSession();

		boolean applied = session.markExpired(NOW);

		assertThat(applied).isTrue();
		assertThat(session.getState()).isEqualTo(CallState.EXPIRED);
		assertThat(session.getTerminalAt()).isEqualTo(NOW);
	}

	@Test
	void markExpiredDoesNotRegressTerminalState() {
		var session = newSession();
		session.markAbandoned(EndReason.DECLINED, DisconnectedBy.CALLER, NOW);

		boolean applied = session.markExpired(NOW.plusSeconds(60));

		assertThat(applied).isFalse();
		assertThat(session.getState()).isEqualTo(CallState.ABANDONED);
		assertThat(session.getTerminalAt()).isEqualTo(NOW);
	}

	@Test
	void markExpiredRejectsNullNow() {
		assertThatNullPointerException().isThrownBy(() -> newSession().markExpired(null));
	}

	@Test
	void createProducesActiveSessionWithInitialGreeting() {
		var session = VoiceCallSession.create("call-leg-uuid", "conversation-uuid");

		assertThat(session.getUuid()).isEqualTo("call-leg-uuid");
		assertThat(session.getConversationUuid()).isEqualTo("conversation-uuid");
		assertThat(session.getState()).isEqualTo(CallState.ACTIVE);
		assertThat(session.getCurrentStep()).isEqualTo(CurrentStep.INITIAL_GREETING);
		assertThat(session.isActive()).isTrue();
		assertThat(session.getTerminalAt()).isNull();
		assertThat(session.getEndReason()).isNull();
		assertThat(session.getDisconnectedBy()).isNull();
	}

	@Test
	void createRejectsNullUuid() {
		assertThatNullPointerException()
			.isThrownBy(() -> VoiceCallSession.create(null, "conversation-uuid"));
	}

	@Test
	void createAsAbandonedProducesAbandonedSession() {
		var session = VoiceCallSession.createAsAbandoned(
			"call-leg-uuid", "conversation-uuid", EndReason.DECLINED, DisconnectedBy.CALLER, NOW);

		assertThat(session.getState()).isEqualTo(CallState.ABANDONED);
		assertThat(session.getEndReason()).isEqualTo(EndReason.DECLINED);
		assertThat(session.getDisconnectedBy()).isEqualTo(DisconnectedBy.CALLER);
		assertThat(session.getTerminalAt()).isEqualTo(NOW);
		assertThat(session.isActive()).isFalse();
	}

	@Test
	void createAsAbandonedRejectsNullTerminalAt() {
		assertThatNullPointerException()
			.isThrownBy(() -> VoiceCallSession.createAsAbandoned("u", "c", EndReason.DECLINED, DisconnectedBy.CALLER, null));
	}

	@Test
	void isActiveIsTrueOnlyForActiveState() {
		var active = VoiceCallSession.create("u", "c");
		var abandoned = VoiceCallSession.createAsAbandoned("u2", "c2", EndReason.DECLINED, DisconnectedBy.CALLER, NOW);

		assertThat(active.isActive()).isTrue();
		assertThat(abandoned.isActive()).isFalse();
	}

	private static VoiceCallSession newSession() {
		return VoiceCallSession.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());
	}
}
