package com.restaurantreceptionist.voice;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "voice_call_sessions")
@EntityListeners(AuditingEntityListener.class)
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
	@Column(name = "current_step", length = 64)
	private CurrentStep currentStep;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@LastModifiedDate
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

	protected VoiceCallSession() {
	}

	public VoiceCallSession(String uuid, String conversationUuid, CallState state, CurrentStep currentStep, String endReason, DisconnectedBy disconnectedBy, Instant terminalAt) {
		Objects.requireNonNull(uuid, "uuid must not be null");
		this.uuid = uuid;
		this.conversationUuid = conversationUuid;
		this.state = state;
		this.currentStep = currentStep;
		this.endReason = endReason;
		this.disconnectedBy = disconnectedBy;
		this.terminalAt = terminalAt;
	}

	public static VoiceCallSession create(String uuid, String conversationUuid){
		return new VoiceCallSession(uuid, conversationUuid, CallState.ACTIVE, CurrentStep.INITIAL_GREETING, null, null, null);
	}

	public static VoiceCallSession createAsAbandoned(String uuid, String conversationUuid, String endReason, DisconnectedBy disconnectedBy, Instant terminalAt) {
		Objects.requireNonNull(terminalAt, "now must not be null");
		return new VoiceCallSession(uuid, conversationUuid, CallState.ABANDONED, null, endReason, disconnectedBy, terminalAt);
	}

	public boolean markAbandoned(String endReason, DisconnectedBy disconnectedBy, Instant now) {
		Objects.requireNonNull(now, "now must not be null");
		if (state != CallState.ACTIVE) {
			return false;
		}
		this.state = CallState.ABANDONED;
		this.endReason = endReason;
		this.disconnectedBy = disconnectedBy;
		this.terminalAt = now;
		return true;
	}

	public boolean markExpired(Instant now) {
		Objects.requireNonNull(now, "now must not be null");
		if (state != CallState.ACTIVE) {
			return false;
		}
		this.state = CallState.EXPIRED;
		this.terminalAt = now;
		return true;
	}

	public boolean isActive() {
		return this.state == CallState.ACTIVE;
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

	public String getCurrentStepName() {
		return currentStep.name();
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
