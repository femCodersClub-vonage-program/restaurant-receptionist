package com.restaurantreceptionist.voice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnswerRequest(
	String uuid,
	@JsonProperty("conversation_uuid") String conversationUuid
) {
}
