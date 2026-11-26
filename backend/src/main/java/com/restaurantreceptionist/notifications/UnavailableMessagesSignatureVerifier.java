package com.restaurantreceptionist.notifications;

import org.springframework.stereotype.Component;

/**
 * Fail-closed default until issue #56 provides the real verifier. Every request is treated as unsigned, so the public endpoint never accepts an unverified callback and Vonage retries later.
 */
@Component
class UnavailableMessagesSignatureVerifier implements MessagesSignatureVerifier {

	@Override
	public MessagesSignatureVerification verify(String authorizationHeader, String rawBody) {
		return new MessagesSignatureVerification.Missing();
	}
}
