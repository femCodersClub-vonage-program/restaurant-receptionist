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
import org.springframework.data.domain.PageRequest;
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
		this.self = self != null ? self : this;
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

	public CallEventResult handleEvent(CallEvent event) {
		Objects.requireNonNull(event, "event must not be null");

		boolean hasStatus = event.status() != null && !event.status().isBlank();
		if (!hasStatus && hasReason(event)) {
			return handleErrorWebhook(event);
		}
		if (!hasStatus) {
			log.warn("Voice event without status or reason acknowledged; no state change (category=UNRECOGNIZED, uuid={}, timestamp={})",
				event.uuid(), event.timestamp());
			return new CallEventResult.Acknowledged();
		}
		if (event.uuid() == null || event.uuid().isBlank()) {
			log.warn("Voice event acknowledged without uuid; no state change (status={}, timestamp={})", event.status(), event.timestamp());
			return new CallEventResult.Acknowledged();
		}

		try {
			return self.applyEvent(event);
		} catch (OptimisticLockingFailureException exception) {
			// The failed transaction has already been rolled back; re-read in a fresh transaction.
			return self.reevaluateTerminal(event);
		}
	}

	@Transactional
	public CallEventResult applyEvent(CallEvent event) {
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
		DisconnectedBy disconnectedBy = DisconnectedBy.map(event.disconnectedBy());
		EndReason endReason = EndReason.fromDetail(event.detail());
		int inserted = repository.insertTerminalIfAbsent(event.uuid(), event.conversationUuid(),
			endReason != null ? endReason.name() : null,
			disconnectedBy != null ? disconnectedBy.name() : null, terminalAt);

		if (inserted == 0) {
			VoiceCallSession call = repository.findByUuid(event.uuid()).orElse(null);
			return call != null && call.isActive()
				? terminateActiveSession(event, call)
				: new CallEventResult.Acknowledged();
		}

		log.info("Terminal tombstone persisted for unknown session (uuid={}, status={}, timestamp={}, endReason={})",
			event.uuid(), event.status(), event.timestamp(), endReason);
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
		log.warn("Provider error webhook acknowledged; no state change (category=PROVIDER_ERROR, conversationUuid={}, timestamp={})",
			event.conversationUuid(), event.timestamp());
		return new CallEventResult.Acknowledged();
	}

	private CallEventResult terminateActiveSession(CallEvent event, VoiceCallSession call) {
		Instant now = event.timestamp() != null ? event.timestamp() : Instant.now(clock);
		boolean applied = call.markAbandoned(EndReason.fromDetail(event.detail()), DisconnectedBy.map(event.disconnectedBy()), now);
		if (!applied) {
			return new CallEventResult.Acknowledged();
		}
		repository.flush();
		log.info("Call session marked abandoned (uuid={}, timestamp={}, endReason={})", event.uuid(), event.timestamp(), call.getEndReason());
		return new CallEventResult.Terminated(call.getState());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public CallEventResult reevaluateTerminal(CallEvent event) {
		VoiceCallSession call = repository.findByUuid(event.uuid()).orElse(null);
		if (call == null || !call.isActive()) {
			return new CallEventResult.Acknowledged();
		}
		Instant now = event.timestamp() != null ? event.timestamp() : Instant.now(clock);
		boolean applied = call.markAbandoned(EndReason.fromDetail(event.detail()), DisconnectedBy.map(event.disconnectedBy()), now);
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
		if (expired > 0) {
			log.info("Expired {} stale voice call session(s)", expired);
		}
		return expired;
	}

	@Transactional
	public int cleanupTerminalSessions() {
		Instant cutoff = Instant.now(clock).minus(properties.retentionPeriod());
		List<VoiceCallSession> terminal = repository.findAllByTerminalAtBefore(cutoff,
			PageRequest.of(0, properties.cleanupBatchSize()));
		if (terminal.isEmpty()) {
			return 0;
		}
		repository.deleteAllInBatch(terminal);
		log.info("Deleted {} voice call session(s) older than the retention period", terminal.size());
		return terminal.size();
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

	private static boolean hasReason(CallEvent event) {
		return event.reason() != null && !event.reason().isBlank();
	}

	private static Ncco greetingNcco() {
		return new Ncco("talk",
			"Le damos la bienvenida a Restaurant Receptionist. Este servicio le permite hacer una reserva por teléfono.",
			"es-ES");
	}
}
