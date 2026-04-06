package com.sentinel.forensics.api;

import com.sentinel.forensics.service.ForensicQueryService;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.SessionSummaryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/forensics/query")
public class ForensicQueryController {

    private final ForensicQueryService forensicQueryService;

    public ForensicQueryController(ForensicQueryService forensicQueryService) {
        this.forensicQueryService = forensicQueryService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionSummaryDTO>> listReplaySessions() {
        return ResponseEntity.ok(forensicQueryService.listReplaySessions());
    }

    @GetMapping("/sessions/{sessionId}/report")
    public ResponseEntity<ReplayReport> getReplayReportBySessionId(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(forensicQueryService.getReplayReportBySessionId(sessionId));
    }

    @GetMapping("/sessions/{sessionId}/verify")
    public ResponseEntity<Boolean> verifyReplayHash(
            @PathVariable UUID sessionId,
            @RequestParam String hash) {
        return ResponseEntity.ok(forensicQueryService.verifyReplayHash(sessionId, hash));
    }

    @GetMapping("/sessions/{sessionId}/verify-hashes")
    public ResponseEntity<List<?>> verifyEventHashes(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(forensicQueryService.verifyEventHashes(sessionId));
    }
}
