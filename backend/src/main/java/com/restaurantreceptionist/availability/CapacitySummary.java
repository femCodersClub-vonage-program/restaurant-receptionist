package com.restaurantreceptionist.availability;

import com.restaurantreceptionist.reservations.DiningSession;

import java.util.Objects;

public record CapacitySummary(
	DiningSession diningSession,
	int totalCapacity,
	int occupiedCapacity,
	int availableCapacity) {

	public CapacitySummary {
		Objects.requireNonNull(diningSession, "diningSession must not be null");
	}

	public static CapacitySummary fromOccupied(DiningSession diningSession, long occupiedGuests) {
		int occupied = Math.toIntExact(occupiedGuests);
		int total = ServiceCapacity.GUESTS_PER_SERVICE;
		return new CapacitySummary(diningSession, total, occupied, total - occupied);
	}
}
