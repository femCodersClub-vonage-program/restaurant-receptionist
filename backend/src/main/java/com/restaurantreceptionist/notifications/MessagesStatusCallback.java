package com.restaurantreceptionist.notifications;

import java.util.Objects;

public record MessagesStatusCallback(String messageUuid, NotificationStatus status, String clientRef) {

	public MessagesStatusCallback {
		if (messageUuid == null || messageUuid.isBlank()) {
			throw new IllegalArgumentException("messageUuid must not be blank");
		}
		Objects.requireNonNull(status, "status must not be null");
		if (!status.isProviderStatus()) {
			throw new IllegalArgumentException("status must be a provider status");
		}
	}
}
