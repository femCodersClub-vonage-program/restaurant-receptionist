package com.restaurantreceptionist.web;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class ProblemDetails {

	private ProblemDetails() {
	}

	public static ProblemDetail forStatusAndDetail(HttpStatus status, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(URI.create("about:blank"));
		return problem;
	}
}
