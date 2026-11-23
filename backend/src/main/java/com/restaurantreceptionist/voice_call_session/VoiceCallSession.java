package com.restaurantreceptionist.voice_call_session;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "voice_call_sessions")
public class VoiceCallSession {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@JdbcTypeCode(SqlTypes.UUID)
	private UUID id;

	@Column(name = "uuid", unique = true, nullable = false)
	private String uuid;

	@Column(name = "conversation_uuid")
	private String conversationUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "state", nullable = false, length = 20)
	private CallState state;

	@Enumerated(EnumType.STRING)
	@Column(name = "current_step")
	private CurrentStep currentStep;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "terminal_at")
	private Instant terminalAt;

	@Column(name = "end_reason")
	private String endReason;

	@Enumerated(EnumType.STRING)
	@Column(name = "disconnected_by", length = 20)
	private DisconnectedBy disconnectedBy;

	@Version
	private Long version;

	public VoiceCallSession(UUID id, String uuid, String conversationUuid, CallState state, CurrentStep currentStep, Instant createdAt, Instant updatedAt, Instant terminalAt, String endReason, DisconnectedBy disconnectedBy, Long version) {
		this.id = id;
		this.uuid = uuid;
		this.conversationUuid = conversationUuid;
		this.state = state;
		this.currentStep = currentStep;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.terminalAt = terminalAt;
		this.endReason = endReason;
		this.disconnectedBy = disconnectedBy;
		this.version = version;
	}

	public VoiceCallSession() {
	}

	public UUID getId() {
		return id;
	}

	public String getUuid() {
		return uuid;
	}

	public String getConversationUuid() {
		return conversationUuid;
	}

	public CallState getState() {
		return state;
	}

	public CurrentStep getCurrentStep() {
		return currentStep;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getTerminalAt() {
		return terminalAt;
	}

	public String getEndReason() {
		return endReason;
	}

	public DisconnectedBy getDisconnectedBy() {
		return disconnectedBy;
	}

	public Long getVersion() {
		return version;
	}
}
