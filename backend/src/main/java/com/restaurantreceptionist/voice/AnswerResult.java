package com.restaurantreceptionist.voice;

public sealed interface AnswerResult {

	record Opened(Ncco ncco) implements AnswerResult {
	}

	record AlreadyTerminal() implements AnswerResult {
	}
}
