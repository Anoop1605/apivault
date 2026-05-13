package com.sentinel.forensics.client;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.forensics.replay.EvaluationResult;

public interface PolicyEngineClient {
    EvaluationResult evaluate(EventDTO event, PolicySnapshot policySnapshot);
}
