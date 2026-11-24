package com.restaurantreceptionist.voice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DisconnectedBy {
	CALLER,
	PLATFORM;

	private static final Logger log = LoggerFactory.getLogger(DisconnectedBy.class);

	public static DisconnectedBy map(String value) {
		if (value == null) {
			return null;
		}
		return switch (value) {
			case "user" -> DisconnectedBy.CALLER;
			case "platform" -> DisconnectedBy.PLATFORM;
			default -> {
				log.warn("Unknown disconnected_by value; ignoring (disconnected_by={})", value);
				yield null;
			}
		};
	}
}
