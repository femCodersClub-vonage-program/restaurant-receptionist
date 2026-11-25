package com.restaurantreceptionist.voice;

import java.time.Instant;

public record CallEvent(
	String uuid,
	String conversationUuid,
	String status,
	String disconnectedBy,
	String reason,
	String detail,
	Instant timestamp
) {
}
