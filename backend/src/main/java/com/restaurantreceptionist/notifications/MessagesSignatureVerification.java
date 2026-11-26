package com.restaurantreceptionist.notifications;

sealed interface MessagesSignatureVerification {

	record Verified() implements MessagesSignatureVerification {
	}

	record Missing() implements MessagesSignatureVerification {
	}

	record Invalid() implements MessagesSignatureVerification {
	}
}
