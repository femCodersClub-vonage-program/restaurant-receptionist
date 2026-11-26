package com.restaurantreceptionist.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

class MessagesStatusRequestTests {

	private final JsonMapper mapper = JsonMapper.builder().build();

	@Test
	void readsDocumentedFieldsAnfIgnoresTheRest() {
		var json = """
			{
			  "message_uuid": "aaaaaaaa-bbbb-cccc-dddd-0123456789ab",
			  "to": "447700900000",
			  "from": "FictionalBistro",
			  "timestamp": "2027-05-14T11:00:00.000Z",
			  "status": "delivered",
			  "channel": "sms",
			  "client_ref": "00000000-0000-0000-0000-000000000001",
			  "usage": { "currency": "EUR", "price": "0.0333" },
			  "sms": { "count_total": "1" }
			}
			""";

		var request = mapper.readValue(json, MessagesStatusRequest.class);

		assertThat(request).isEqualTo(new MessagesStatusRequest(
			"aaaaaaaa-bbbb-cccc-dddd-0123456789ab",
			"delivered",
			"sms",
			"00000000-0000-0000-0000-000000000001"));
	}

	@Test
	void readsMissingAndNullFieldsAsNull() {
		var request = mapper.readValue("{\"status\": null}", MessagesStatusRequest.class);

		assertThat(request).isEqualTo(new MessagesStatusRequest(null, null, null, null));
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
		"message_uuid | 123",
		"status       | 123",
		"status       | true",
		"status       | {}",
		"status       | []",
		"channel      | false",
		"client_ref   | 42"
	})
	void rejectsNonStringValues(String field, String jsonValue) {
		var json = "{\"" + field + "\": " + jsonValue + "}";

		assertThatThrownBy(() -> mapper.readValue(json, MessagesStatusRequest.class))
			.isInstanceOf(MismatchedInputException.class);
	}
}
