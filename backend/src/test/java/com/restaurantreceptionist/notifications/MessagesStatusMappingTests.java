package com.restaurantreceptionist.notifications;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class MessagesStatusMappingTests {

	@ParameterizedTest
	@CsvSource({
		"submitted, SUBMITTED",
		"delivered, DELIVERED",
		"rejected, REJECTED",
		"undeliverable, UNDELIVERABLE"
	})
	void mapsDocumentedStatuses(String vonageStatus, NotificationStatus expected) {
		assertThat(MessagesStatusMapping.toNotificationStatus(vonageStatus)).contains(expected);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "read", "failed", "DELIVERED", " delivered", "accepted" })
	void ignoresUnsupportedStatuses(String vonageStatus) {
		assertThat(MessagesStatusMapping.toNotificationStatus(vonageStatus)).isEmpty();
	}
}
