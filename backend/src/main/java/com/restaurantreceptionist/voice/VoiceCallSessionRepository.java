package com.restaurantreceptionist.voice;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoiceCallSessionRepository extends JpaRepository<VoiceCallSession, UUID> {

	Optional<VoiceCallSession> findByUuid(String uuid);

	List<VoiceCallSession> findAllByStateAndUpdatedAtBefore(CallState state, Instant updatedAt);

	List<VoiceCallSession> findAllByTerminalAtBefore(Instant terminalAt, Pageable pageable);

	@Modifying
	@Query(value = """
		INSERT INTO voice_call_sessions (uuid, conversation_uuid, state, current_step)
		VALUES (:uuid, :conversationUuid, :state, :currentStep)
		ON CONFLICT (uuid) DO NOTHING
		""", nativeQuery = true)
	int insertIfAbsent(@Param("uuid") String uuid, @Param("conversationUuid") String conversationUuid, String state, String currentStep);

	@Modifying
	@Query(value = """
		INSERT INTO voice_call_sessions (uuid, conversation_uuid, state, end_reason, disconnected_by, terminal_at)
		VALUES (:uuid, :conversationUuid, 'ABANDONED', :endReason, :disconnectedBy, :terminalAt)
		ON CONFLICT (uuid) DO NOTHING
		""", nativeQuery = true)
	int insertTerminalIfAbsent(@Param("uuid") String uuid, @Param("conversationUuid") String conversationUuid,
		@Param("endReason") String endReason, @Param("disconnectedBy") String disconnectedBy,
		@Param("terminalAt") Instant terminalAt);
}
