package com.restaurantreceptionist.voice;

public sealed interface CallEventResult {

	record Acknowledged() implements CallEventResult {
	}

	record Terminated(CallState state) implements CallEventResult {
	}
}
