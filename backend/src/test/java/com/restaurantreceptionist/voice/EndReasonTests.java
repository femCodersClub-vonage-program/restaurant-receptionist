package com.restaurantreceptionist.voice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class EndReasonTests {

	@ParameterizedTest
	@CsvSource({
		"invalid_number, INVALID_NUMBER",
		"restricted, RESTRICTED",
		"declined, DECLINED",
		"throttled, THROTTLED",
		"cannot_route, CANNOT_ROUTE",
		"number_out_of_service, NUMBER_OUT_OF_SERVICE",
		"internal_error, INTERNAL_ERROR"
	})
	void mapsDocumentedVonageDetailValues(String detail, EndReason expected) {
		assertThat(EndReason.fromDetail(detail)).isEqualTo(expected);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "", "   " })
	void mapsAbsentDetailToNull(String detail) {
		assertThat(EndReason.fromDetail(detail)).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = { "some_provider_value", "invalid_number_extra", "DECLINED" })
	void mapsUndocumentedDetailToUnrecognized(String detail) {
		assertThat(EndReason.fromDetail(detail)).isEqualTo(EndReason.UNRECOGNIZED);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"throttled, see knowledge article https://api.support.vonage.com/hc/en-us/articles/207100288",
		"throttled, see knowledge article https://example.com"
	})
	void mapsThrottledWithTrailingTextToThrottled(String detail) {
		assertThat(EndReason.fromDetail(detail)).isEqualTo(EndReason.THROTTLED);
	}
}
