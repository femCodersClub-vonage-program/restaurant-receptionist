package com.restaurantreceptionist.reservations;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

class StrictLocalTimeDeserializer extends ValueDeserializer<LocalTime> {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm")
		.withResolverStyle(ResolverStyle.STRICT);

	@Override
	public LocalTime deserialize(JsonParser parser, DeserializationContext context) {
		if (parser.currentToken() != JsonToken.VALUE_STRING) {
			return context.reportInputMismatch(this, "Expected a HH:mm string");
		}
		try {
			return LocalTime.parse(parser.getString(), FORMAT);
		} catch (DateTimeParseException exception) {
			return context.reportInputMismatch(this, "Expected a valid 24-hour time in HH:mm format");
		}
	}
}
