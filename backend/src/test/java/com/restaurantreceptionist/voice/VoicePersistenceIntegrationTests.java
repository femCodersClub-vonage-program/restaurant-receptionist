package com.restaurantreceptionist.voice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import com.restaurantreceptionist.TestcontainersConfiguration;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class VoicePersistenceIntegrationTests {

	@Autowired
	private Flyway flyway;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private VoiceCallSessionRepository voiceCallSessionRepository;

	@Test
	void appliesVoiceCallSessionsMigration() {
		assertThat(flyway.info().applied())
				.extracting(migration -> migration.getVersion().getVersion())
				.contains("3");
		assertThat(jdbcTemplate.queryForObject(
				"SELECT to_regclass('public.voice_call_sessions') IS NOT NULL", Boolean.class)).isTrue();
	}

	@Test
	void persistsAndRetrievesActiveVoiceCallSession() {
		var session = VoiceCallSession.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());

		var saved = voiceCallSessionRepository.saveAndFlush(session);
		var retrieved = voiceCallSessionRepository.findById(saved.getId()).orElseThrow();

		assertThat(retrieved.getUuid()).isEqualTo(session.getUuid());
		assertThat(retrieved.getConversationUuid()).isEqualTo(session.getConversationUuid());
		assertThat(retrieved.getState()).isEqualTo(CallState.ACTIVE);
		assertThat(retrieved.getCurrentStep()).isEqualTo(CurrentStep.INITIAL_GREETING);
		assertThat(retrieved.getCreatedAt()).isNotNull();
		assertThat(retrieved.getUpdatedAt()).isNotNull();
		assertThat(retrieved.getTerminalAt()).isNull();
		assertThat(retrieved.getEndReason()).isNull();
		assertThat(retrieved.getDisconnectedBy()).isNull();
	}

	@Test
	void persistsAbandonedSession() {
		var now = Instant.parse("2027-05-14T13:30:00Z");
		var session = VoiceCallSession.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		assertThat(session.markAbandoned("caller hung up", DisconnectedBy.CALLER, now)).isTrue();

		var saved = voiceCallSessionRepository.saveAndFlush(session);
		var retrieved = voiceCallSessionRepository.findById(saved.getId()).orElseThrow();

		assertThat(retrieved.getState()).isEqualTo(CallState.ABANDONED);
		assertThat(retrieved.getEndReason()).isEqualTo("caller hung up");
		assertThat(retrieved.getDisconnectedBy()).isEqualTo(DisconnectedBy.CALLER);
		assertThat(retrieved.getTerminalAt()).isEqualTo(now);
	}

	@Test
	void persistsExpiredSession() {
		var now = Instant.parse("2027-05-14T13:30:00Z");
		var session = VoiceCallSession.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		assertThat(session.markExpired(now)).isTrue();

		var saved = voiceCallSessionRepository.saveAndFlush(session);
		var retrieved = voiceCallSessionRepository.findById(saved.getId()).orElseThrow();

		assertThat(retrieved.getState()).isEqualTo(CallState.EXPIRED);
		assertThat(retrieved.getTerminalAt()).isEqualTo(now);
		assertThat(retrieved.getEndReason()).isNull();
		assertThat(retrieved.getDisconnectedBy()).isNull();
	}

	@Test
	void rejectsNullUuid() {
		assertThatThrownBy(() -> jdbcTemplate.update("""
				INSERT INTO voice_call_sessions (uuid, conversation_uuid, state, current_step)
				VALUES (NULL, ?, 'ACTIVE', 'INITIAL_GREETING')
				""", UUID.randomUUID().toString()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void enforcesUniqueUuid() {
		var uuid = UUID.randomUUID().toString();
		voiceCallSessionRepository.saveAndFlush(VoiceCallSession.create(uuid, UUID.randomUUID().toString()));

		assertThatThrownBy(() -> voiceCallSessionRepository.saveAndFlush(
				VoiceCallSession.create(uuid, UUID.randomUUID().toString())))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsUnsupportedState() {
		assertThatThrownBy(() -> jdbcTemplate.update("""
				INSERT INTO voice_call_sessions (uuid, conversation_uuid, state, current_step)
				VALUES (?, ?, 'RINGING', 'INITIAL_GREETING')
				""", UUID.randomUUID().toString(), UUID.randomUUID().toString()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void incrementsVersionOnUpdate() {
		var session = VoiceCallSession.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		var saved = voiceCallSessionRepository.saveAndFlush(session);

		assertThat(saved.getVersion()).isZero();

		saved.markAbandoned("caller hung up", DisconnectedBy.CALLER, Instant.parse("2027-05-14T13:30:00Z"));
		voiceCallSessionRepository.saveAndFlush(saved);

		assertThat(saved.getVersion()).isEqualTo(1L);
	}
}
