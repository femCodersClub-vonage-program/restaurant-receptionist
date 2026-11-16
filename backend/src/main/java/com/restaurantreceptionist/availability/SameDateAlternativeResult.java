package com.restaurantreceptionist.availability;

public sealed interface SameDateAlternativeResult {

	record Available(CapacitySummary capacity) implements SameDateAlternativeResult {
	}

	record Unavailable(
		CapacitySummary capacity,
		int requestedPartySize
	) implements SameDateAlternativeResult {
	}

	record InvalidPartySize(int partySize) implements SameDateAlternativeResult {
	}
}
