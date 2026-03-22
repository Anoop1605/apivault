package com.sentinel.shared.dto;

import java.util.UUID;

public class SessionSummaryDTO {

    private UUID sessionId;
    private long startTimestampNs;
    private long endTimestampNs;
    private int eventCount;
    private String hash;

    public SessionSummaryDTO() {}

    public SessionSummaryDTO(UUID sessionId, long startTimestampNs, long endTimestampNs,
                              int eventCount, String hash) {
        this.sessionId = sessionId;
        this.startTimestampNs = startTimestampNs;
        this.endTimestampNs = endTimestampNs;
        this.eventCount = eventCount;
        this.hash = hash;
    }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public long getStartTimestampNs() { return startTimestampNs; }
    public void setStartTimestampNs(long startTimestampNs) { this.startTimestampNs = startTimestampNs; }

    public long getEndTimestampNs() { return endTimestampNs; }
    public void setEndTimestampNs(long endTimestampNs) { this.endTimestampNs = endTimestampNs; }

    public int getEventCount() { return eventCount; }
    public void setEventCount(int eventCount) { this.eventCount = eventCount; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
}
