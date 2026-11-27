package com.restaurantreceptionist.voice;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnswerRequest(
	String uuid,
	@JsonProperty("conversation_uuid") String conversationUuid
) {
}
