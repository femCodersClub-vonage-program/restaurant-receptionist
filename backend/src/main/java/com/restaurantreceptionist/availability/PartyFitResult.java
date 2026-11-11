package com.restaurantreceptionist.availability;

import java.time.LocalTime;

public sealed interface PartyFitResult {

	record Fits(CapacitySummary capacity) implements PartyFitResult {
	}

	record InvalidPartySize(int partySize) implements PartyFitResult {
	}

	record InvalidArrivalTime(LocalTime arrivalTime) implements PartyFitResult {
	}

	record InsufficientCapacity(
		CapacitySummary capacity,
		int requestedPartySize
	) implements PartyFitResult {
	}
}
