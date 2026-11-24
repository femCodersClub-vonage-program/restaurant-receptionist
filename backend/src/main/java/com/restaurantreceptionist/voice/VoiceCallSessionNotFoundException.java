package com.restaurantreceptionist.voice;

public class VoiceCallSessionNotFoundException extends RuntimeException {

	public VoiceCallSessionNotFoundException(String uuid) {
		super("Voice call session not found for uuid: " + uuid);
	}
}
