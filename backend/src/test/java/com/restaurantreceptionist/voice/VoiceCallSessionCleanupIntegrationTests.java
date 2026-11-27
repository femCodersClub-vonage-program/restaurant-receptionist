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
@SpringBootTest(properties = {
	"voice.retention-period=PT1M",
	"voice.cleanup-batch-size=50" })
@Transactional
class VoiceCallSessionCleanupIntegrationTests {

	@Autowired
	private VoiceCallSessionService voiceCallSessionService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void deletesTerminalSessionsOlderThanRetentionButKeepsRecentOnes() {
		var oldUuid = UUID.randomUUID().toString();
		var recentUuid = UUID.randomUUID().toString();

		jdbcTemplate.update("""
			INSERT INTO voice_call_sessions (uuid, conversation_uuid, state, current_step, terminal_at)
			VALUES (?, NULL, 'ABANDONED', NULL, CURRENT_TIMESTAMP - interval '10 minutes')
			""", oldUuid);
		jdbcTemplate.update("""
			INSERT INTO voice_call_sessions (uuid, conversation_uuid, state, current_step, terminal_at)
			VALUES (?, NULL, 'ABANDONED', NULL, CURRENT_TIMESTAMP)
			""", recentUuid);

		int removed = voiceCallSessionService.cleanupTerminalSessions();

		assertThat(removed).isEqualTo(1);
		assertThat(count(oldUuid)).isZero();
		assertThat(count(recentUuid)).isEqualTo(1);
	}

	private Integer count(String uuid) {
		return jdbcTemplate.queryForObject(
			"SELECT count(*) FROM voice_call_sessions WHERE uuid = ?", Integer.class, uuid);
	}
}
