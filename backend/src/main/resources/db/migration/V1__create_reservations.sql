CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_first_name TEXT NOT NULL,
    customer_last_name TEXT NOT NULL,
    phone_number TEXT NOT NULL,
    reservation_date DATE NOT NULL,
    arrival_time TIME WITHOUT TIME ZONE NOT NULL,
    service VARCHAR(10) NOT NULL,
    party_size SMALLINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_reservations_service
        CHECK (service IN ('LUNCH', 'DINNER')),
    CONSTRAINT chk_reservations_party_size
        CHECK (party_size BETWEEN 1 AND 30),
    CONSTRAINT chk_reservations_status
        CHECK (status = 'CONFIRMED')
);

CREATE INDEX idx_reservations_date_service_arrival_time
    ON reservations (reservation_date, service, arrival_time);
