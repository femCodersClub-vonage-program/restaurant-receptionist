package com.restaurantreceptionist.webhooks;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vonage.client.voice.VoiceClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

import static com.restaurantreceptionist.webhooks.WebhookSignatureVerification.INVALID_SIGNATURE;
import static com.restaurantreceptionist.webhooks.WebhookSignatureVerification.MISSING_SIGNATURE;
import static com.restaurantreceptionist.webhooks.WebhookSignatureVerification.VERIFIED;

final class VonageWebhookSignatureVerifier implements WebhookSignatureVerifier {

	private static final Pattern BEARER_HEADER = Pattern.compile(
		"(?i:Bearer) +([A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+)");
	private static final Pattern SHA256_HEX = Pattern.compile("[0-9a-fA-F]{64}");

	private final String apiKey;
	private final String signatureSecret;
	private final String applicationId;

	VonageWebhookSignatureVerifier(String apiKey, String signatureSecret, String applicationId) {
		this.apiKey = apiKey;
		this.signatureSecret = signatureSecret;
		this.applicationId = applicationId;
	}

	@Override
	public WebhookSignatureVerification verify(String authorizationHeader, String rawBody) {
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			return MISSING_SIGNATURE;
		}

		var bearer = BEARER_HEADER.matcher(authorizationHeader.strip());
		if (!bearer.matches()) {
			return INVALID_SIGNATURE;
		}

		String token = bearer.group(1);
		// The SDK helper verifies the same HS256 JWT format used by Voice and Messages.
		try {
			if (!VoiceClient.verifySignature(token, signatureSecret)) {
				return INVALID_SIGNATURE;
			}
		} catch (NullPointerException malformedToken) {
			// The SDK can dereference a null header while reading a malformed JWT.
			return INVALID_SIGNATURE;
		}

		DecodedJWT verifiedToken = JWT.decode(token);
		Claim application = verifiedToken.getClaim("application_id");
		boolean expectedIdentity = "Vonage".equals(verifiedToken.getIssuer())
			&& apiKey.equals(verifiedToken.getClaim("api_key").asString())
			&& (application.isMissing() || applicationId.equals(application.asString()));
		if (!expectedIdentity || !matchesPayload(verifiedToken.getClaim("payload_hash"), rawBody)) {
			return INVALID_SIGNATURE;
		}

		return VERIFIED;
	}

	private static boolean matchesPayload(Claim payloadHash, String rawBody) {
		if (rawBody == null) {
			return false;
		}
		if (payloadHash.isMissing()) {
			return true;
		}

		String expectedHash = payloadHash.asString();
		return expectedHash != null && SHA256_HEX.matcher(expectedHash).matches()
			&& MessageDigest.isEqual(HexFormat.of().parseHex(expectedHash), sha256(rawBody));
	}

	private static byte[] sha256(String rawBody) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(rawBody.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}
}
