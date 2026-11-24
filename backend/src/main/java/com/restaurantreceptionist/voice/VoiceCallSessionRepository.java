package com.restaurantreceptionist.voice;

import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoiceCallSessionRepository extends JpaRepository<VoiceCallSession, UUID> {
}
