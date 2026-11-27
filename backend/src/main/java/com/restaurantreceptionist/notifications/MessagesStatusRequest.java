package com.restaurantreceptionist.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
record MessagesStatusRequest(
	@JsonProperty("message_uuid")
	@JsonDeserialize(using = StrictStringDeserializer.class)
	String messageUuid,
	@JsonDeserialize(using = StrictStringDeserializer.class)
	String status,
	@JsonDeserialize(using = StrictStringDeserializer.class)
	String channel,
	@JsonProperty("client_ref")
	@JsonDeserialize(using = StrictStringDeserializer.class)
	String clientRef) {
}
