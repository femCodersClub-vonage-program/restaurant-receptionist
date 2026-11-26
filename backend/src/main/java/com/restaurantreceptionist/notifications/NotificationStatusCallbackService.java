package com.restaurantreceptionist.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationStatusCallbackService {

	private static final Logger log = LoggerFactory.getLogger(NotificationStatusCallbackService.class);

	private final ReservationNotificationRepository repository;

	public NotificationStatusCallbackService(ReservationNotificationRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public StatusCallbackOutcome apply(MessagesStatusCallback callback) {
		Objects.requireNonNull(callback, "callback must not be null");
		Optional<UUID> clientRefId = parseClientRef(callback.clientRef());

		Optional<ReservationNotification> byMessageUuid = repository.findForUpdateByProviderMessageId(callback.messageUuid());

		if (byMessageUuid.isPresent()) {
			ReservationNotification notification = byMessageUuid.get();
			if (clientRefId.isPresent() && !clientRefId.get().equals(notification.getId())) {
				return uncorrelated(callback, StatusCallbackOutcome.AMBIGUOUS_CORRELATION);
			}
			return applyTo(notification, callback);
		}

		if (clientRefId.isEmpty()) {
			return uncorrelated(callback, StatusCallbackOutcome.UNKNOWN_CORRELATION);
		}

		return repository.findForUpdateById(clientRefId.get())
			.map(notification -> applyTo(notification, callback))
			.orElseGet(() -> uncorrelated(callback, StatusCallbackOutcome.UNKNOWN_CORRELATION));
	}

	private StatusCallbackOutcome applyTo(ReservationNotification notification, MessagesStatusCallback callback) {
		boolean changed = notification.applyProviderStatus(callback.status(), callback.messageUuid());
		StatusCallbackOutcome outcome = changed ? StatusCallbackOutcome.APPLIED : StatusCallbackOutcome.IGNORED;
		log.info("Messages status {} for notification {}: {}", callback.status(), notification.getId(), outcome);
		return outcome;
	}

	private StatusCallbackOutcome uncorrelated(MessagesStatusCallback callback, StatusCallbackOutcome outcome) {
		log.warn("Messages status {} not applied: {}", callback.status(), outcome);
		return outcome;
	}

	private static Optional<UUID> parseClientRef(String clientRef) {
		if (clientRef == null) {
			return Optional.empty();
		}
		try {
			UUID id = UUID.fromString(clientRef);
			return id.toString().equalsIgnoreCase(clientRef) ? Optional.of(id) : Optional.empty();
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

}
