package com.restaurantreceptionist.voice;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class DevelopmentSignatureVerifier implements SignatureVerifier {

	@Override
	public SignatureVerification verify(HttpServletRequest request) {
		return new SignatureVerification.Verified();
	}
}
