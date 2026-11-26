package com.restaurantreceptionist.notifications;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

class StrictStringDeserializer extends ValueDeserializer<String> {

	@Override
	public String deserialize(JsonParser parser, DeserializationContext context) {
		if (parser.currentToken() != JsonToken.VALUE_STRING) {
			return context.reportInputMismatch(this, "Expected a JSON string");
		}
		return parser.getString();
	}
}
