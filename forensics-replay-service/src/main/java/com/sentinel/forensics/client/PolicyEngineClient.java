package com.sentinel.forensics.client;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.forensics.replay.EvaluationResult;

import java.util.List;
import java.util.UUID;

public interface PolicyEngineClient {
    EvaluationResult evaluate(EventDTO event, PolicySnapshot policySnapshot);

    default PolicySnapshot fetchSnapshot(UUID snapshotId) {
        return new PolicySnapshot(List.of());
    }
}
