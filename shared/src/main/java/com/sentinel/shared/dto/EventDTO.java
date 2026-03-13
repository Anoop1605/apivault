package com.sentinel.shared.dto;

import java.util.UUID;

public class EventDTO {
    private UUID sessionId;
    private long timestampNs;
    // Add other fields as needed

    public UUID getSessionId() {
        return sessionId;
    }
    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }
    public long getTimestampNs() {
        return timestampNs;
    }
    public void setTimestampNs(long timestampNs) {
        this.timestampNs = timestampNs;
    }
}
