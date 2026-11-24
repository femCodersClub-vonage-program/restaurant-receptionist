package com.restaurantreceptionist.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ReservationNotificationTests {

	private static final UUID RESERVATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	@Test
	void createsNotificationsInTheirInitialStates() {
		assertThat(ReservationNotification.notRequestedCannotReceive(RESERVATION_ID).getStatus())
			.isEqualTo(NotificationStatus.NOT_REQUESTED_CANNOT_RECEIVE);
		assertThat(ReservationNotification.notRequestedCapabilityUnknown(RESERVATION_ID).getStatus())
			.isEqualTo(NotificationStatus.NOT_REQUESTED_CAPABILITY_UNKNOWN);

		var pending = ReservationNotification.pending(RESERVATION_ID);

		assertThat(pending.getStatus()).isEqualTo(NotificationStatus.PENDING);
		assertThat(pending.getReservationId()).isEqualTo(RESERVATION_ID);
		assertThat(pending.getProviderMessageId()).isNull();
		assertThat(pending.getFailureReason()).isNull();
	}

	@Test
	void rejectsMissingReservationId() {
		assertThatThrownBy(() -> ReservationNotification.pending(null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void marksPendingNotificationAsAccepted() {
		var notification = ReservationNotification.pending(RESERVATION_ID);

		assertThat(notification.markAccepted("message-1")).isTrue();
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.ACCEPTED);
		assertThat(notification.getProviderMessageId()).isEqualTo("message-1");
	}

	@Test
	void rejectsBlankMessageIdWithoutChangingState() {
		var notification = ReservationNotification.pending(RESERVATION_ID);

		assertThatThrownBy(() -> notification.markAccepted(" "))
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
		assertThat(notification.getProviderMessageId()).isNull();
	}

	@ParameterizedTest
	@EnumSource(value = NotificationFailureReason.class, names = {"PROVIDER_REQUEST_REJECTED", "PROVIDER_UNAVAILABLE"})
	void marksPendingNotificationAsFailedWhenRequestIsKnownNotAccepted(NotificationFailureReason reason) {
		var notification = ReservationNotification.pending(RESERVATION_ID);

		assertThat(notification.markRequestFailed(reason)).isTrue();

		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.REQUEST_FAILED);
		assertThat(notification.getFailureReason()).isEqualTo(reason);
	}

	@Test
	void rejectsOutcomeUnknownReasonForFailedRequest() {
		var notification = ReservationNotification.pending(RESERVATION_ID);

		assertThatThrownBy(() -> notification.markRequestFailed(NotificationFailureReason.PROVIDER_OUTCOME_UNKNOWN))
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
		assertThat(notification.getFailureReason()).isNull();
	}

	@Test
	void marksPendingNotificationAsOutcomeUnknown() {
		var notification = ReservationNotification.pending(RESERVATION_ID);

		assertThat(notification.markOutcomeUnknown()).isTrue();
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.REQUEST_OUTCOME_UNKNOWN);
		assertThat(notification.getFailureReason()).isEqualTo(NotificationFailureReason.PROVIDER_OUTCOME_UNKNOWN);
	}

	@Test
	void rejectsRequestSideTransitionsWhenNotPending() {
		var notification = ReservationNotification.notRequestedCannotReceive(RESERVATION_ID);

		assertThatThrownBy(() -> notification.markAccepted("message-1"))
			.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> notification.markRequestFailed(NotificationFailureReason.PROVIDER_UNAVAILABLE))
			.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(notification::markOutcomeUnknown)
			.isInstanceOf(IllegalStateException.class);
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.NOT_REQUESTED_CANNOT_RECEIVE);
	}

	@Test
	void appliesProviderStatusesInOrder() {
		var notification = accepted("message-1");

		assertThat(notification.applyProviderStatus(NotificationStatus.SUBMITTED, "message-1")).isTrue();
		assertThat(notification.applyProviderStatus(NotificationStatus.DELIVERED, "message-1")).isTrue();

		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
	}

	@Test
	void ignoresRepeatedProviderStatus() {
		var notification = accepted("message-1");

		notification.applyProviderStatus(NotificationStatus.SUBMITTED, "message-1");

		assertThat(notification.applyProviderStatus(NotificationStatus.SUBMITTED, "message-1")).isFalse();
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SUBMITTED);
	}

	@ParameterizedTest
	@EnumSource(value = NotificationStatus.class, names = { "DELIVERED", "REJECTED", "UNDELIVERABLE" })
	void ignoresProviderStatusAfterFinalState(NotificationStatus finalStatus) {
		var notification = accepted("message-1");
		assertThat(notification.applyProviderStatus(finalStatus, "message-1")).isTrue();

		assertThat(notification.applyProviderStatus(NotificationStatus.SUBMITTED, "message-1")).isFalse();
		assertThat(notification.getStatus()).isEqualTo(finalStatus);
		assertThat(notification.getProviderMessageId()).isEqualTo("message-1");
	}

	@Test
	void resolvesOutcomeUnknownWithCallbackMessageId() {
		var notification = ReservationNotification.pending(RESERVATION_ID);
		notification.markOutcomeUnknown();

		assertThat(notification.applyProviderStatus(NotificationStatus.DELIVERED, "message-2")).isTrue();

		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
		assertThat(notification.getProviderMessageId()).isEqualTo("message-2");
		assertThat(notification.getFailureReason()).isNull();
	}

	@Test
	void rejectsOutcomeUnknownResolutionWithoutMessageId() {
		var notification = ReservationNotification.pending(RESERVATION_ID);
		notification.markOutcomeUnknown();

		assertThatThrownBy(() -> notification.applyProviderStatus(NotificationStatus.DELIVERED, null))
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.REQUEST_OUTCOME_UNKNOWN);
		assertThat(notification.getFailureReason()).isEqualTo(NotificationFailureReason.PROVIDER_OUTCOME_UNKNOWN);
	}

	@Test
	void ignoresProviderStatusWithDifferentMessageId() {
		var notification = accepted("message-1");

		assertThat(notification.applyProviderStatus(NotificationStatus.DELIVERED, "message-2")).isFalse();
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.ACCEPTED);
		assertThat(notification.getProviderMessageId()).isEqualTo("message-1");
	}

	@Test
	void resolvesPendingWithEarlyCallback() {
		var notification = ReservationNotification.pending(RESERVATION_ID);

		assertThat(notification.applyProviderStatus(NotificationStatus.DELIVERED, "message-1")).isTrue();

		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
		assertThat(notification.getProviderMessageId()).isEqualTo("message-1");
		assertThat(notification.getFailureReason()).isNull();
	}

	@Test
	void rejectsEarlyCallbackWithoutMessageId() {
		var notification = ReservationNotification.pending(RESERVATION_ID);

		assertThatThrownBy(() -> notification.applyProviderStatus(NotificationStatus.SUBMITTED, " "))
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
		assertThat(notification.getProviderMessageId()).isNull();
	}

	@Test
	void ignoresLateAcceptanceWithSameMessageId() {
		var notification = resolvedByEarlyCallback(NotificationStatus.DELIVERED);

		assertThat(notification.markAccepted("message-1")).isFalse();

		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
		assertThat(notification.getProviderMessageId()).isEqualTo("message-1");
	}

	@Test
	void rejectsLateAcceptanceWithDifferentMessageIdWithoutChangingState() {
		var notification = resolvedByEarlyCallback(NotificationStatus.SUBMITTED);

		assertThatThrownBy(() -> notification.markAccepted("message-2"))
			.isInstanceOf(IllegalStateException.class);
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SUBMITTED);
		assertThat(notification.getProviderMessageId()).isEqualTo("message-1");
	}

	@Test
	void ignoresLateFailedOrAmbiguousOutcomeAfterCallback() {
		var notification = resolvedByEarlyCallback(NotificationStatus.SUBMITTED);

		assertThat(notification.markRequestFailed(NotificationFailureReason.PROVIDER_REQUEST_REJECTED)).isFalse();
		assertThat(notification.markOutcomeUnknown()).isFalse();

		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SUBMITTED);
		assertThat(notification.getProviderMessageId()).isEqualTo("message-1");
		assertThat(notification.getFailureReason()).isNull();
	}

	@Test
	void rejectsInvalidFailureReasonEvenAfterCallback() {
		var notification = resolvedByEarlyCallback(NotificationStatus.DELIVERED);

		assertThatThrownBy(() -> notification.markRequestFailed(NotificationFailureReason.PROVIDER_OUTCOME_UNKNOWN))
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
	}

	@ParameterizedTest
	@EnumSource(value = NotificationStatus.class, names = { "SUBMITTED", "DELIVERED", "REJECTED", "UNDELIVERABLE" })
	void endsInSameStateWhicheverArrivesFirst(NotificationStatus providerStatus) {
		var callbackFirst = resolvedByEarlyCallback(providerStatus);
		callbackFirst.markAccepted("message-1");

		var acceptanceFirst = accepted("message-1");
		acceptanceFirst.applyProviderStatus(providerStatus, "message-1");

		assertThat(callbackFirst.getStatus()).isEqualTo(providerStatus);
		assertThat(acceptanceFirst.getStatus()).isEqualTo(providerStatus);
		assertThat(callbackFirst.getProviderMessageId()).isEqualTo("message-1");
		assertThat(acceptanceFirst.getProviderMessageId()).isEqualTo("message-1");
	}

	@ParameterizedTest
	@EnumSource(value = NotificationStatus.class, mode = EnumSource.Mode.EXCLUDE,
		names = { "SUBMITTED", "DELIVERED", "REJECTED", "UNDELIVERABLE" })
	void rejectsNonProviderStatus(NotificationStatus status) {
		var notification = accepted("message-1");

		assertThatThrownBy(() -> notification.applyProviderStatus(status, "message-1"))
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.ACCEPTED);
	}

	@Test
	void rejectsNullProviderStatus() {
		var notification = accepted("message-1");

		assertThatThrownBy(() -> notification.applyProviderStatus(null, "message-1"))
			.isInstanceOf(NullPointerException.class);
	}

	private static ReservationNotification accepted(String messageId) {
		var notification = ReservationNotification.pending(RESERVATION_ID);
		notification.markAccepted(messageId);
		return notification;
	}

	private static ReservationNotification resolvedByEarlyCallback(NotificationStatus providerStatus) {
		var notification = ReservationNotification.pending(RESERVATION_ID);
		notification.applyProviderStatus(providerStatus, "message-1");
		return notification;
	}
}
