package com.restaurantreceptionist.webhooks;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
class VonageWebhookVerificationConfiguration {

	@Bean
	WebhookSignatureVerifier webhookSignatureVerifier(Environment environment) {
		if (!environment.getProperty("vonage.webhooks.enabled", Boolean.class, false)) {
			return (authorizationHeader, rawBody) -> WebhookSignatureVerification.MISSING_SIGNATURE;
		}

		return new VonageWebhookSignatureVerifier(
			requiredSetting(environment, "vonage.webhooks.api-key", "VONAGE_API_KEY"),
			requiredSetting(environment, "vonage.webhooks.signature-secret", "VONAGE_SIGNATURE_SECRET"),
			requiredSetting(environment, "vonage.webhooks.application-id", "VONAGE_APPLICATION_ID"));
	}

	private static String requiredSetting(Environment environment, String property, String variable) {
		String value = environment.getProperty(property);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Vonage webhook verification requires " + variable);
		}
		return value;
	}
}
