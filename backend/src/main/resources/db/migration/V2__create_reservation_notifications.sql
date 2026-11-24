CREATE TABLE reservation_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID NOT NULL,
    status VARCHAR(40) NOT NULL,
    provider_message_id TEXT,
    failure_reason VARCHAR(40),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_notifications_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations (id),
    CONSTRAINT uq_reservation_notifications_reservation
        UNIQUE (reservation_id),
    CONSTRAINT uq_reservation_notifications_provider_message
        UNIQUE (provider_message_id),
    CONSTRAINT chk_reservation_notifications_status
        CHECK (status IN (
            'NOT_REQUESTED_CANNOT_RECEIVE',
            'NOT_REQUESTED_CAPABILITY_UNKNOWN',
            'PENDING',
            'REQUEST_FAILED',
            'REQUEST_OUTCOME_UNKNOWN',
            'ACCEPTED',
            'SUBMITTED',
            'DELIVERED',
            'REJECTED',
            'UNDELIVERABLE'
    )),
    CONSTRAINT chk_reservation_notifications_provider_message
        CHECK ((provider_message_id IS NOT NULL) = (status IN (
            'ACCEPTED', 'SUBMITTED', 'DELIVERED', 'REJECTED', 'UNDELIVERABLE'
    ))),
    CONSTRAINT chk_reservation_notifications_failure_reason
        CHECK (
            (status = 'REQUEST_FAILED'
                AND failure_reason IS NOT NULL
                AND failure_reason IN ('PROVIDER_REQUEST_REJECTED', 'PROVIDER_UNAVAILABLE'))
            OR (status = 'REQUEST_OUTCOME_UNKNOWN'
                AND failure_reason IS NOT NULL
                AND failure_reason = 'PROVIDER_OUTCOME_UNKNOWN')
            OR (status NOT IN ('REQUEST_FAILED', 'REQUEST_OUTCOME_UNKNOWN')
                AND failure_reason IS NULL)
        )
);
