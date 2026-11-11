package com.restaurantreceptionist.availability;

import com.restaurantreceptionist.reservations.DiningSession;

import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

public final class ArrivalWindows {

	public static final LocalTime LUNCH_START = LocalTime.of(13, 0);

	public static final LocalTime LUNCH_END = LocalTime.of(16, 0);

	public static final LocalTime DINNER_START = LocalTime.of(20, 0);

	public static final LocalTime DINNER_END = LocalTime.of(23, 30);

	private ArrivalWindows() {
	}

	public static Optional<DiningSession> classify(LocalTime arrivalTime) {
		Objects.requireNonNull(arrivalTime, "arrivalTime must not be null");

		if (isWithin(arrivalTime, LUNCH_START, LUNCH_END)) {
			return Optional.of(DiningSession.LUNCH);
		}
		if (isWithin(arrivalTime, DINNER_START, DINNER_END)) {
			return Optional.of(DiningSession.DINNER);
		}
		return Optional.empty();
	}

	private static boolean isWithin(LocalTime time, LocalTime start, LocalTime end) {
		return !time.isBefore(start) && !time.isAfter(end);
	}
}
