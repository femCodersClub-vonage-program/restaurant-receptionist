package com.restaurantreceptionist.voice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DisconnectedByTests {

	@ParameterizedTest
	@CsvSource({
		"user, CALLER",
		"platform, PLATFORM"
	})
	void mapsDocumentedVonageValues(String value, DisconnectedBy expected) {
		assertThat(DisconnectedBy.map(value)).isEqualTo(expected);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "", "unknown", "caller", "system", "   " })
	void mapsAbsentOrUndocumentedValuesToNull(String value) {
		assertThat(DisconnectedBy.map(value)).isNull();
	}
}
