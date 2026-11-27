package com.restaurantreceptionist.webhooks;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/** Creates signed requests locally using only fictional values. */
public final class VonageWebhookFixtures {
	public static final String API_KEY = "fictional-account";
	public static final String APPLICATION_ID = "00000000-0000-0000-0000-000000000056";
	public static final String SIGNATURE_SECRET = "fictional-webhook-test-secret-only";

	private VonageWebhookFixtures() {
	}

	public static String authorizationFor(String rawBody) {
		return authorization(tokenBuilder()
			.withClaim("application_id", APPLICATION_ID)
			.withClaim("payload_hash", payloadHash(rawBody)));
	}

	static JWTCreator.Builder tokenBuilder() {
		return JWT.create()
			.withIssuer("Vonage")
			.withClaim("api_key", API_KEY)
			.withIssuedAt(Instant.EPOCH)
			.withJWTId("fictional-webhook-jti");
	}

	static String authorization(JWTCreator.Builder builder) {
		return "Bearer " + builder.sign(Algorithm.HMAC256(SIGNATURE_SECRET));
	}

	private static String payloadHash(String rawBody) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawBody.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new AssertionError(exception);
		}
	}
}
