package com.restaurantreceptionist.reservations;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

class StrictIntegerDeserializer extends ValueDeserializer<Integer> {

	@Override
	public Integer deserialize(JsonParser parser, DeserializationContext context) {
		if (
			parser.currentToken() != JsonToken.VALUE_NUMBER_INT
			|| parser.getNumberType() != JsonParser.NumberType.INT) {
			return context.reportInputMismatch(this, "Expected a JSON integer within the int range");
		}
		return parser.getIntValue();
	}
}
