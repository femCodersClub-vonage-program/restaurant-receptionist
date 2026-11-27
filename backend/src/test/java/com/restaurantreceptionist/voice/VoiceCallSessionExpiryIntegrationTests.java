package com.restaurantreceptionist.voice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantreceptionist.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "voice.inactivity-timeout=PT1M")
@Transactional
class VoiceCallSessionExpiryIntegrationTests {

	@Autowired
	private VoiceCallSessionService voiceCallSessionService;

	@Autowired
	private VoiceCallSessionRepository voiceCallSessionRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void expiresStaleActiveSessionButKeepsRecentOne() {
		var staleUuid = UUID.randomUUID().toString();
		var freshUuid = UUID.randomUUID().toString();

		jdbcTemplate.update("""
			INSERT INTO voice_call_sessions (uuid, conversation_uuid, state, current_step, updated_at)
			VALUES (?, ?, 'ACTIVE', 'INITIAL_GREETING', CURRENT_TIMESTAMP - interval '10 minutes')
			""", staleUuid, null);
		jdbcTemplate.update("""
			INSERT INTO voice_call_sessions (uuid, conversation_uuid, state, current_step)
			VALUES (?, ?, 'ACTIVE', 'INITIAL_GREETING')
			""", freshUuid, null);

		int expired = voiceCallSessionService.expireStaleSessions();

		assertThat(expired).isEqualTo(1);
		assertThat(voiceCallSessionRepository.findByUuid(staleUuid).orElseThrow().getState())
			.isEqualTo(CallState.EXPIRED);
		assertThat(voiceCallSessionRepository.findByUuid(staleUuid).orElseThrow().getTerminalAt()).isNotNull();
		assertThat(voiceCallSessionRepository.findByUuid(freshUuid).orElseThrow().getState())
			.isEqualTo(CallState.ACTIVE);
	}
}
