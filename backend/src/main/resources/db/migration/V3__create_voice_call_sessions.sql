CREATE TABLE voice_call_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uuid TEXT UNIQUE NOT NULL,
    conversation_uuid TEXT,
    state VARCHAR(20) NOT NULL,
    current_step VARCHAR(60),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    terminal_at TIMESTAMP WITH TIME ZONE,
    end_reason TEXT,
    disconnected_by VARCHAR(20),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_voice_call_sessions_state
        CHECK (state IN ('ACTIVE', 'ABANDONED', 'COMPLETED', 'EXPIRED'))
);
CREATE INDEX idx_voice_call_conversation_uuid
    ON voice_call_sessions (conversation_uuid);
CREATE INDEX idx_voice_call_terminal_at
    ON voice_call_sessions (terminal_at);
