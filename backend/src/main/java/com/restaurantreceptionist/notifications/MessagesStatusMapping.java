package com.restaurantreceptionist.notifications;

import java.util.Optional;

final class MessagesStatusMapping {

	private MessagesStatusMapping() {
	}

	static Optional<NotificationStatus> toNotificationStatus(String vonageStatus) {
		if (vonageStatus == null) {
			return Optional.empty();
		}
		return switch (vonageStatus) {
			case "submitted" -> Optional.of(NotificationStatus.SUBMITTED);
			case "delivered" -> Optional.of(NotificationStatus.DELIVERED);
			case "rejected" -> Optional.of(NotificationStatus.REJECTED);
			case "undeliverable" -> Optional.of(NotificationStatus.UNDELIVERABLE);
			default -> Optional.empty();
		};
	}
}
