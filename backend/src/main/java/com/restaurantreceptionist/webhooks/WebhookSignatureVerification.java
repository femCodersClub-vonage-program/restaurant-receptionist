package com.restaurantreceptionist.webhooks;

public enum WebhookSignatureVerification {
	VERIFIED,
	MISSING_SIGNATURE,
	INVALID_SIGNATURE
}
