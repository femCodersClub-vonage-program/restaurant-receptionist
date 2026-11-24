package com.restaurantreceptionist.voice;

public record CallEvent(
	String uuid,
	String conversationUuid,
	String status,
	String disconnectedBy,
	String reason) {
}
