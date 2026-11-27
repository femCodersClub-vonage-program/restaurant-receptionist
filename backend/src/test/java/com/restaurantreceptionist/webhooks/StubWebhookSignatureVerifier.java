package com.restaurantreceptionist.webhooks;

import java.util.Objects;

/** Returns a fixed outcome for a consumer test without running signature verification. */
public record StubWebhookSignatureVerifier(WebhookSignatureVerification outcome) implements WebhookSignatureVerifier {

	public StubWebhookSignatureVerifier {
		Objects.requireNonNull(outcome);
	}

	@Override
	public WebhookSignatureVerification verify(String authorizationHeader, String rawBody) {
		return outcome;
	}
}
