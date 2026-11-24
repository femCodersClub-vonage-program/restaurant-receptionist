package com.restaurantreceptionist.voice;

public sealed interface SignatureVerification {

	record Verified() implements SignatureVerification {
	}

	record Missing() implements SignatureVerification {
	}

	record Invalid() implements SignatureVerification {
	}
}
