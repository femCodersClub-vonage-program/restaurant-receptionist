package com.restaurantreceptionist.notifications;

/**
 * Provisional verification contract for Messages status callbacks, to be replaced by the shared Vonage webhook verifier from issue #56.
 */
interface MessagesSignatureVerifier {

	MessagesSignatureVerification verify(String authorizationHeader, String rawBody);
}
