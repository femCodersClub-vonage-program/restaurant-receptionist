package com.restaurantreceptionist.notifications;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservation_notifications")
public class ReservationNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "reservation_id", nullable = false, updatable = false)
	private UUID reservationId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private NotificationStatus status;

	@Column(name = "provider_message_id")
	private String providerMessageId;

	@Enumerated(EnumType.STRING)
	@Column(name = "failure_reason", length = 40)
	private NotificationFailureReason failureReason;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected ReservationNotification() {
	}

	private ReservationNotification(UUID reservationId, NotificationStatus status) {
		this.reservationId = Objects.requireNonNull(reservationId, "reservationId must not be null");
		this.status = status;
	}

	public static ReservationNotification notRequestedCannotReceive(UUID reservationId) {
		return new ReservationNotification(reservationId, NotificationStatus.NOT_REQUESTED_CANNOT_RECEIVE);
	}

	public static ReservationNotification notRequestedCapabilityUnknown(UUID reservationId) {
		return new ReservationNotification(reservationId, NotificationStatus.NOT_REQUESTED_CAPABILITY_UNKNOWN);
	}

	public static ReservationNotification pending(UUID reservationId) {
		return new ReservationNotification(reservationId, NotificationStatus.PENDING);
	}

	public void markAccepted(String providerMessageId) {
		requireStatus(NotificationStatus.PENDING);
		this.providerMessageId = requireMessageId(providerMessageId);
		this.status = NotificationStatus.ACCEPTED;
	}

	public void markRequestFailed(NotificationFailureReason reason) {
		requireStatus(NotificationStatus.PENDING);
		if (reason != NotificationFailureReason.PROVIDER_REQUEST_REJECTED && reason != NotificationFailureReason.PROVIDER_UNAVAILABLE) {
			throw new IllegalArgumentException("A failed request requires a known-not-accepted reason, but was " + reason);
		}
		this.failureReason = reason;
		this.status = NotificationStatus.REQUEST_FAILED;
	}

	public void markOutcomeUnknown() {
		requireStatus(NotificationStatus.PENDING);
		this.failureReason = NotificationFailureReason.PROVIDER_OUTCOME_UNKNOWN;
		this.status = NotificationStatus.REQUEST_OUTCOME_UNKNOWN;
	}

	/**
	 * Applies a status reported by a Messages status callback.
	 *
	 * @return {@code true} if the notification changed, {@code false} if the status was ignored
	 */
	public boolean applyProviderStatus(NotificationStatus providerStatus, String providerMessageId) {
		Objects.requireNonNull(providerStatus, "providerStatus must not be null");

		if (!providerStatus.isProviderStatus()) throw new IllegalArgumentException("providerStatus must be a provider status");

		if (status.isFinal() || status == providerStatus || status == NotificationStatus.PENDING) {
			return false;
		}

		if (status == NotificationStatus.REQUEST_OUTCOME_UNKNOWN) {
			this.providerMessageId = requireMessageId(providerMessageId);
			this.failureReason = null;
		}

		if (status == NotificationStatus.ACCEPTED || status == NotificationStatus.SUBMITTED) {
			if (!Objects.equals(this.providerMessageId, providerMessageId)) {
				return false;
			}
		}

		this.status = providerStatus;
		return true;
	}

	private void requireStatus(NotificationStatus expected) {
		if (status != expected) {
			throw new IllegalStateException("Expected notification status " + expected + " but was " + status);
		}
	}

	private static String requireMessageId(String providerMessageId) {
		if (providerMessageId == null || providerMessageId.isBlank()) {
			throw new IllegalArgumentException("providerMessageId must not be blank");
		}
		return providerMessageId;
	}

	public UUID getId() {
		return id;
	}

	public UUID getReservationId() {
		return reservationId;
	}

	public NotificationStatus getStatus() {
		return status;
	}

	public String getProviderMessageId() {
		return providerMessageId;
	}

	public NotificationFailureReason getFailureReason() {
		return failureReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
