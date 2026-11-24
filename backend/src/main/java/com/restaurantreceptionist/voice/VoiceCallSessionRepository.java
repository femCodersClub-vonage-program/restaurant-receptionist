package com.restaurantreceptionist.voice;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VoiceCallSessionRepository extends JpaRepository<VoiceCallSession, UUID> {
}
