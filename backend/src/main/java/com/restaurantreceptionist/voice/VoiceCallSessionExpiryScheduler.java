package com.restaurantreceptionist.voice;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VoiceCallSessionExpiryScheduler {

	private final VoiceCallSessionService voiceCallSessionService;

	public VoiceCallSessionExpiryScheduler(VoiceCallSessionService voiceCallSessionService) {
		this.voiceCallSessionService = voiceCallSessionService;
	}

	@Scheduled(fixedDelayString = "${voice.expiry-scan-interval:PT1M}")
	public void expireStaleSessions() {
		voiceCallSessionService.expireStaleSessions();
	}
}
