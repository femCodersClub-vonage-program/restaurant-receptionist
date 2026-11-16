package com.restaurantreceptionist.reservations;

public enum DiningSession {
	LUNCH,
	DINNER;

	public DiningSession opposite() {
		return switch (this) {
			case LUNCH -> DINNER;
			case DINNER -> LUNCH;
		};
	}
}
