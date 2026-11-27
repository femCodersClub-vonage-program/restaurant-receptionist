package com.restaurantreceptionist.voice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class VoiceFailureMessageTests {

	@Test
	void usesSpanishFromSpainForEveryMessage() {
		assertThat(VoiceFailureMessage.LANGUAGE).isEqualTo("es-ES");
	}

	@ParameterizedTest
	@EnumSource(VoiceFailureMessage.class)
	void everyMessageHasSpokenText(VoiceFailureMessage message) {
		assertThat(message.text()).isNotBlank();
	}

	@ParameterizedTest
	@EnumSource(VoiceFailureMessage.class)
	void noMessageImpliesAConfirmedReservationOrPromisesAnSms(VoiceFailureMessage message) {
		String text = message.text().toLowerCase(Locale.ROOT);

		assertThat(text).doesNotContain("confirmad", "queda reservad", "sms", "mensaje");
	}

	@Test
	void retryConfirmationStatesThatTheReservationIsNotCompletedYet() {
		assertThat(VoiceFailureMessage.RETRY_CONFIRMATION.text()).contains("no se ha completado");
	}

	@Test
	void endCallStatesThatNoReservationWasMade() {
		assertThat(VoiceFailureMessage.END_CALL.text()).contains("ninguna reserva");
	}
}
