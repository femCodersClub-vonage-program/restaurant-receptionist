package com.restaurantreceptionist.webhooks;

@FunctionalInterface
public interface WebhookSignatureVerifier {

	/**
	 * Verifies a request before its body is parsed or any state changes are made.
	 *
	 * @param authorizationHeader the received Authorization header, or null if absent
	 * @param rawBody the original UTF-8 body as text, or an empty string for a bodyless request
	 * @return the verification outcome; the caller decides how to respond
	 */
	WebhookSignatureVerification verify(String authorizationHeader, String rawBody);
}
