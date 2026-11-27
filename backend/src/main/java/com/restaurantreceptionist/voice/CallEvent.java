package com.restaurantreceptionist.voice;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CallEvent(
	String uuid,
	@JsonProperty("conversation_uuid") String conversationUuid,
	String status,
	@JsonProperty("disconnected_by") String disconnectedBy,
	String reason,
	String detail,
	Instant timestamp
) {
}
