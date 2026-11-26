package com.restaurantreceptionist.webhooks;

import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static com.restaurantreceptionist.webhooks.VonageWebhookFixtures.API_KEY;
import static com.restaurantreceptionist.webhooks.VonageWebhookFixtures.APPLICATION_ID;
import static com.restaurantreceptionist.webhooks.VonageWebhookFixtures.SIGNATURE_SECRET;
import static com.restaurantreceptionist.webhooks.VonageWebhookFixtures.authorization;
import static com.restaurantreceptionist.webhooks.VonageWebhookFixtures.authorizationFor;
import static com.restaurantreceptionist.webhooks.VonageWebhookFixtures.tokenBuilder;
import static com.restaurantreceptionist.webhooks.WebhookSignatureVerification.INVALID_SIGNATURE;
import static com.restaurantreceptionist.webhooks.WebhookSignatureVerification.MISSING_SIGNATURE;
import static com.restaurantreceptionist.webhooks.WebhookSignatureVerification.VERIFIED;
import static org.assertj.core.api.Assertions.assertThat;

class VonageWebhookSignatureVerifierTests {
	private static final String BODY = "{ \"label\": \"men\u00fa\" }\n";
	private final WebhookSignatureVerifier verifier = new VonageWebhookSignatureVerifier(
		API_KEY, SIGNATURE_SECRET, APPLICATION_ID);

	@Test
	void acceptsLocallySignedCallbackWithMatchingApplicationAndExactUtf8Body() {
		assertThat(verifier.verify(authorizationFor(BODY), BODY)).isEqualTo(VERIFIED);
	}

	@Test
	void acceptsSignedRequestWithoutBody() {
		assertThat(verifier.verify(authorizationFor(""), "")).isEqualTo(VERIFIED);
		assertThat(verifier.verify(authorization(tokenBuilder()), "")).isEqualTo(VERIFIED);
	}

	@Test
	void permitsMissingApplicationIdAndExpiry() {
		assertThat(verifier.verify(authorization(tokenBuilder()), BODY)).isEqualTo(VERIFIED);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"  \t"})
	void distinguishesMissingAuthorization(String header) {
		assertThat(verifier.verify(header, BODY)).isEqualTo(MISSING_SIGNATURE);
	}

	@ParameterizedTest
	@ValueSource(strings = {"Basic abc", "Bearer", "Bearer ", "Bearer not-a-jwt", "Bearer abc.def.ghi"})
	void rejectsMalformedAuthorization(String header) {
		assertThat(verifier.verify(header, BODY)).isEqualTo(INVALID_SIGNATURE);
	}

	@Test
	void acceptsCaseInsensitiveBearerScheme() {
		String header = authorizationFor(BODY).replaceFirst("Bearer", "bearer");
		assertThat(verifier.verify(header, BODY)).isEqualTo(VERIFIED);
	}

	@Test
	void rejectsWrongSecret() {
		String header = "Bearer " + tokenBuilder().sign(Algorithm.HMAC256("another-fictional-secret"));
		assertThat(verifier.verify(header, BODY)).isEqualTo(INVALID_SIGNATURE);
	}

	@Test
	void rejectsDifferentSigningAlgorithm() {
		String header = "Bearer " + tokenBuilder().sign(Algorithm.HMAC512(SIGNATURE_SECRET));
		assertThat(verifier.verify(header, BODY)).isEqualTo(INVALID_SIGNATURE);
	}

	@Test
	void rejectsExpiredToken() {
		String header = authorization(tokenBuilder().withExpiresAt(Instant.EPOCH.plusSeconds(1)));
		assertThat(verifier.verify(header, BODY)).isEqualTo(INVALID_SIGNATURE);
	}

	@Test
	void rejectsOtherOrMissingAccount() {
		assertThat(verifier.verify(authorization(tokenBuilder().withClaim("api_key", "another-fictional-account")), BODY))
			.isEqualTo(INVALID_SIGNATURE);
		assertThat(verifier.verify(authorization(tokenBuilder().withClaim("api_key", (String) null)), BODY))
			.isEqualTo(INVALID_SIGNATURE);
	}

	@Test
	void rejectsOtherOrMissingIssuer() {
		assertThat(verifier.verify(authorization(tokenBuilder().withIssuer("Another issuer")), BODY))
			.isEqualTo(INVALID_SIGNATURE);
		assertThat(verifier.verify(authorization(tokenBuilder().withIssuer(null)), BODY))
			.isEqualTo(INVALID_SIGNATURE);
	}

	@Test
	void rejectsMismatchedOrMalformedApplicationIdWhenPresent() {
		assertThat(verifier.verify(authorization(tokenBuilder().withClaim("application_id", "other-application")), BODY))
			.isEqualTo(INVALID_SIGNATURE);
		assertThat(verifier.verify(authorization(tokenBuilder().withNullClaim("application_id")), BODY))
			.isEqualTo(INVALID_SIGNATURE);
	}

	@Test
	void rejectsModifiedBodyEvenWhenJsonMeansTheSameThing() {
		assertThat(verifier.verify(authorizationFor(BODY), BODY.strip())).isEqualTo(INVALID_SIGNATURE);
	}

	@Test
	void rejectsMalformedPayloadHash() {
		String header = authorization(tokenBuilder().withClaim("payload_hash", "not-a-sha256-digest"));
		assertThat(verifier.verify(header, BODY)).isEqualTo(INVALID_SIGNATURE);
	}
}
