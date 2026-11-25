package com.restaurantreceptionist.voice;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantreceptionist.config.VoiceProperties;

@Service
public class VoiceCallSessionService {

	private static final Logger log = LoggerFactory.getLogger(VoiceCallSessionService.class);
	private static final Set<String> TERMINAL_STATUSES = Set.of("completed", "rejected", "failed");
	private final VoiceCallSessionRepository repository;
	private final Clock clock;
	private final VoiceProperties properties;
	private final VoiceCallSessionService self;

	public VoiceCallSessionService(VoiceCallSessionRepository repository, Clock clock, VoiceProperties properties,
			@Lazy VoiceCallSessionService self) {
		this.repository = repository;
		this.clock = clock;
		this.properties = properties;
		this.self = self;
	}

	@Transactional
	public AnswerResult answer(String uuid, String conversationUuid) {

		VoiceCallSession call = VoiceCallSession.create(uuid, conversationUuid);
		repository.insertIfAbsent(call.getUuid(), call.getConversationUuid(), call.getState().name(), call.getCurrentStepName());

		VoiceCallSession winner = repository.findByUuid(uuid)
			.orElseThrow(() -> new VoiceCallSessionNotFoundException(uuid));

		return winner.isActive()
			? new AnswerResult.Opened(greetingNcco())
			: new AnswerResult.AlreadyTerminal();
	}

	@Transactional
	public CallEventResult handleEvent(CallEvent event) {
		Objects.requireNonNull(event, "event must not be null");

		if (isErrorWebhook(event)) {
			return handleErrorWebhook(event);
		}
		if (event.uuid() == null || event.uuid().isBlank()) {
			log.warn("Voice event acknowledged without uuid; no state change (status={}, timestamp={})", event.status(), event.timestamp());
			return new CallEventResult.Acknowledged();
		}

		return repository.findByUuid(event.uuid())
			.map(call -> handleExistingSession(event, call))
			.orElseGet(() -> handleUnknownSession(event));
	}

	private CallEventResult handleUnknownSession(CallEvent event) {
		if (isNonTerminal(event)) {
			logNonTerminal(event, false, "Voice event for unknown session acknowledged; no session created (uuid={}, status={}, timestamp={})");
			return new CallEventResult.Acknowledged();
		}

		Instant terminalAt = event.timestamp() != null ? event.timestamp() : Instant.now(clock);
		VoiceCallSession tombstone = VoiceCallSession.createAsAbandoned(event.uuid(), event.conversationUuid(), event.detail(), DisconnectedBy.map(event.disconnectedBy()), terminalAt);
		repository.saveAndFlush(tombstone);
		log.info("Terminal tombstone persisted for unknown session (uuid={}, status={}, timestamp={}, detail={})", event.uuid(), event.status(), event.timestamp(), event.detail());
		return new CallEventResult.Terminated(CallState.ABANDONED);
	}

	private CallEventResult handleExistingSession(CallEvent event, VoiceCallSession call) {
		if (isNonTerminal(event)) {
			logNonTerminal(event, true, "Voice event acknowledged with no state change (uuid={}, status={}, timestamp={})");
			return new CallEventResult.Acknowledged();
		}
		return terminateActiveSession(event, call);
	}

	private CallEventResult handleErrorWebhook(CallEvent event) {
		log.warn("Provider error webhook received (reason={}, conversationUuid={}, timestamp={})", event.reason(),
			event.conversationUuid(), event.timestamp());
		return new CallEventResult.Acknowledged();
	}

	private CallEventResult terminateActiveSession(CallEvent event, VoiceCallSession call) {
		try {
			Instant now = event.timestamp() != null ? event.timestamp() : Instant.now(clock);
			boolean applied = call.markAbandoned(event.detail(), DisconnectedBy.map(event.disconnectedBy()), now);
			if (!applied) {
				return new CallEventResult.Acknowledged();
			}
			repository.flush();
			log.info("Call session marked abandoned (uuid={}, timestamp={}, detail={})", event.uuid(), event.timestamp(), event.detail());
			return new CallEventResult.Terminated(call.getState());
		} catch (OptimisticLockingFailureException exception) {
			return self.reevaluateTerminal(event);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public CallEventResult reevaluateTerminal(CallEvent event) {
		VoiceCallSession call = repository.findByUuid(event.uuid()).orElse(null);
		if (call == null || !call.isActive()) {
			return new CallEventResult.Acknowledged();
		}
		Instant now = event.timestamp() != null ? event.timestamp() : Instant.now(clock);
		boolean applied = call.markAbandoned(event.detail(), DisconnectedBy.map(event.disconnectedBy()), now);
		return applied
			? new CallEventResult.Terminated(call.getState())
			: new CallEventResult.Acknowledged();
	}

	@Transactional
	public int expireStaleSessions() {
		Instant cutoff = Instant.now(clock).minus(properties.inactivityTimeout());
		List<VoiceCallSession> stale = repository.findAllByStateAndUpdatedAtBefore(CallState.ACTIVE, cutoff);
		int expired = 0;
		for (VoiceCallSession session : stale) {
			if (session.markExpired(Instant.now(clock))) {
				expired++;
			}
		}
		return expired;
	}

	private static boolean isNonTerminal(CallEvent event) {
		return !TERMINAL_STATUSES.contains(event.status());
	}

	private static boolean isQuietNonTerminal(String status, boolean sessionExists) {
		return "transfer".equals(status) || ("answered".equals(status) && sessionExists);
	}

	private static void logNonTerminal(CallEvent event, boolean sessionExists, String format) {
		if (!isQuietNonTerminal(event.status(), sessionExists)) {
			log.info(format, event.uuid(), event.status(), event.timestamp());
		}
	}

	private static boolean isErrorWebhook(CallEvent event) {
		return (event.status() == null || event.status().isBlank())
			&& event.reason() != null && !event.reason().isBlank();
	}

	private static Ncco greetingNcco() {
		return new Ncco("talk",
			"Le damos la bienvenida a Restaurant Receptionist. Este servicio le permite hacer una reserva por teléfono.",
			"es-ES");
	}
}
