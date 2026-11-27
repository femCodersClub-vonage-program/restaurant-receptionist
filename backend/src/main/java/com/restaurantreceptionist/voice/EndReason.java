package com.restaurantreceptionist.voice;

/**
 * Known diagnostic categories for a terminated call session.
 *
 * <p>These are the only values stored in {@code end_reason}. The raw Vonage
 * {@code detail} and {@code reason} strings are mapped to one of these
 * categories and are never copied into logs or persisted data.</p>
 *
 * <p>The stored categories cover the terminal statuses whose {@code detail}
 * is persisted as {@code end_reason}: {@code rejected}
 * ({@code invalid_number}, {@code restricted}, {@code declined},
 * {@code throttled}) and {@code failed} ({@code cannot_route},
 * {@code number_out_of_service}, {@code internal_error}). The
 * {@code unanswered} status also carries {@code detail} values
 * ({@code unavailable}, {@code timeout}), but it is non-terminal and its
 * {@code detail} is never persisted.</p>
 */
public enum EndReason {

	INVALID_NUMBER,
	RESTRICTED,
	DECLINED,
	THROTTLED,
	CANNOT_ROUTE,
	NUMBER_OUT_OF_SERVICE,
	INTERNAL_ERROR,
	UNRECOGNIZED;

	/**
	 * Maps a documented Vonage {@code detail} value to a category.
	 *
	 * @param detail the raw detail value, or null when absent
	 * @return the matching category, {@code UNRECOGNIZED} for an undocumented
	 *         value, or {@code null} when the detail is absent
	 */
	public static EndReason fromDetail(String detail) {
		if (detail == null || detail.isBlank()) {
			return null;
		}
		// CPS throttling can append extra text after the value, for example
		// "throttled, see knowledge article https://...". Match on the leading
		// token so such values still resolve to THROTTLED.
		String token = detail.strip().split("[, ]", 2)[0];
		return switch (token) {
			case "invalid_number" -> INVALID_NUMBER;
			case "restricted" -> RESTRICTED;
			case "declined" -> DECLINED;
			case "throttled" -> THROTTLED;
			case "cannot_route" -> CANNOT_ROUTE;
			case "number_out_of_service" -> NUMBER_OUT_OF_SERVICE;
			case "internal_error" -> INTERNAL_ERROR;
			default -> UNRECOGNIZED;
		};
	}
}
