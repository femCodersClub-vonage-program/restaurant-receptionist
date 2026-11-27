package com.restaurantreceptionist.voice;

/**
 * Spanish prompts spoken after a backend or provider failure.
 *
 * <p>A failure never consumes a caller-input attempt (RN-05) and never implies that a reservation was confirmed (RN-06). The calling flow decides which message applies and appends the question to repeat.</p>
 */
public enum VoiceFailureMessage {

	RETRY_STEP("Lo sentimos, hemos tenido un problema técnico. Le repetimos la última pregunta."),
	RETRY_CONFIRMATION("Lo sentimos, hemos tenido un problema técnico y su reserva todavía no se ha completado. Le repetimos la pregunta."),
	END_CALL("Lo sentimos, ahora mismo no podemos completar su reserva por un problema técnico. No se ha realizado ninguna reserva. Por favor, vuelva a llamar más tarde.");

	public static final String LANGUAGE = "es-ES";

	private final String text;

	VoiceFailureMessage(String text) {
		this.text = text;
	}

	public String text() {
		return text;
	}
}
