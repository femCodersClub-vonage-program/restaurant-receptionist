package com.restaurantreceptionist.notifications;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class VerifiedMessagesSignatureTestConfiguration {

	@Bean
	@Primary
	MessagesSignatureVerifier verifiedMessagesSignatureVerifier() {
		return (authorizationHeader, rawBody) -> new MessagesSignatureVerification.Verified();
	}
}
