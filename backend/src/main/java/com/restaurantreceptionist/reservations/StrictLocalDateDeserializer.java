package com.restaurantreceptionist.reservations;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

class StrictLocalDateDeserializer extends ValueDeserializer<LocalDate> {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd")
		.withResolverStyle(ResolverStyle.STRICT);

	@Override
	public LocalDate deserialize(JsonParser parser, DeserializationContext context) {
		if (parser.currentToken() != JsonToken.VALUE_STRING) {
			return context.reportInputMismatch(this, "Expected a YYYY-MM-DD string");
		}
		try {
			return LocalDate.parse(parser.getString(), FORMAT);
		} catch (DateTimeParseException exception) {
			return context.reportInputMismatch(this, "Expected a valid calendar date in YYYY-MM-DD format");
		}
	}
}
