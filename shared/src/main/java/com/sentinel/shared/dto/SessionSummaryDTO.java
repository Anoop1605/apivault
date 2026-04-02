package com.sentinel.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryDTO {

    private UUID sessionId;
    private long startTimestampNs;
    private long endTimestampNs;
    private int eventCount;
    private String hash;

}

