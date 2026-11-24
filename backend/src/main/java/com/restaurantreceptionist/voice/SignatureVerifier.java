package com.restaurantreceptionist.voice;

import jakarta.servlet.http.HttpServletRequest;

public interface SignatureVerifier {

	SignatureVerification verify(HttpServletRequest request);
}
