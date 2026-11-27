package com.restaurantreceptionist.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MessagesStatusCallbackTests {

	private static final String MESSAGE_UUID = "00000000-0000-0000-0000-0000000000aa";

	@Test
	void acceptsCallbackWithoutClientRef() {
		var callback = new MessagesStatusCallback(MESSAGE_UUID, NotificationStatus.DELIVERED, null);

		assertThat(callback.clientRef()).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "  " })
	void rejectsBlankMessageUuid(String messageUuid) {
		assertThatThrownBy(() -> new MessagesStatusCallback(messageUuid, NotificationStatus.DELIVERED, null))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void rejectsMissingStatus() {
		assertThatThrownBy(() -> new MessagesStatusCallback(MESSAGE_UUID, null, null))
			.isInstanceOf(NullPointerException.class);
	}

	@ParameterizedTest
	@EnumSource(value = NotificationStatus.class, names = { "SUBMITTED", "DELIVERED", "REJECTED", "UNDELIVERABLE" }, mode = EnumSource.Mode.EXCLUDE)
	void rejectsNonProviderStatuses(NotificationStatus status) {
		assertThatThrownBy(() -> new MessagesStatusCallback(MESSAGE_UUID, status, null))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
