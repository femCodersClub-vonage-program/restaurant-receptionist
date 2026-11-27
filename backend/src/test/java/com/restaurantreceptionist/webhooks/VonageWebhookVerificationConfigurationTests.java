package com.restaurantreceptionist.webhooks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;

import static com.restaurantreceptionist.webhooks.VonageWebhookFixtures.API_KEY;
import static com.restaurantreceptionist.webhooks.VonageWebhookFixtures.APPLICATION_ID;
import static com.restaurantreceptionist.webhooks.VonageWebhookFixtures.SIGNATURE_SECRET;
import static com.restaurantreceptionist.webhooks.VonageWebhookFixtures.authorizationFor;
import static com.restaurantreceptionist.webhooks.WebhookSignatureVerification.MISSING_SIGNATURE;
import static com.restaurantreceptionist.webhooks.WebhookSignatureVerification.VERIFIED;
import static org.assertj.core.api.Assertions.assertThat;

class VonageWebhookVerificationConfigurationTests {
	private static final String BODY = "{}";
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withInitializer(context -> context.getEnvironment().getPropertySources()
			.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME))
		.withInitializer(new ConfigDataApplicationContextInitializer())
		.withUserConfiguration(VonageWebhookVerificationConfiguration.class);

	@Test
	void startsWithARejectingVerifierAndNoCredentialsByDefault() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(WebhookSignatureVerifier.class);
			var verifier = context.getBean(WebhookSignatureVerifier.class);
			assertThat(verifier.verify(authorizationFor(BODY), BODY)).isEqualTo(MISSING_SIGNATURE);
		});
	}

	@Test
	void enablesRealVerificationWithConfiguredValues() {
		enabledWithSettings().run(context -> {
			assertThat(context).hasSingleBean(WebhookSignatureVerifier.class);
			var verifier = context.getBean(WebhookSignatureVerifier.class);
			assertThat(verifier.verify(authorizationFor(BODY), BODY)).isEqualTo(VERIFIED);
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {"VONAGE_API_KEY", "VONAGE_SIGNATURE_SECRET", "VONAGE_APPLICATION_ID"})
	void failsAtStartupWithTheNameOfTheMissingSetting(String variable) {
		enabledWithSettings().withPropertyValues(variable + "=").run(context -> {
			assertThat(context).hasFailed();
			assertThat(context.getStartupFailure())
				.hasRootCauseInstanceOf(IllegalStateException.class)
				.hasRootCauseMessage("Vonage webhook verification requires " + variable);
		});
	}

	private ApplicationContextRunner enabledWithSettings() {
		return contextRunner.withPropertyValues(
			"VONAGE_WEBHOOKS_ENABLED=true",
			"VONAGE_API_KEY=" + API_KEY,
			"VONAGE_SIGNATURE_SECRET=" + SIGNATURE_SECRET,
			"VONAGE_APPLICATION_ID=" + APPLICATION_ID);
	}
}
