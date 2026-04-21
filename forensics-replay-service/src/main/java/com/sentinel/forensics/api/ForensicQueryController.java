package com.sentinel.forensics.api;

import com.sentinel.forensics.hash.HashVerificationUtil.VerificationResult;
import com.sentinel.forensics.service.ForensicQueryService;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.SessionSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/forensics/query")
@Tag(name = "Forensic Query", description = "Query sessions, reports, and hash verification")
public class ForensicQueryController {

    private final ForensicQueryService forensicQueryService;

    public ForensicQueryController(ForensicQueryService forensicQueryService) {
        this.forensicQueryService = forensicQueryService;
    }

    @Operation(summary = "List all sessions",
               description = "Returns summary metadata for all known replay sessions.")
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionSummaryDTO>> listReplaySessions() {
        return ResponseEntity.ok(forensicQueryService.listReplaySessions());
    }

    @Operation(summary = "Get replay report",
               description = "Returns the full replay report for a session including " +
                             "per-step decisions and tamper-evident hash.")
    @GetMapping("/sessions/{sessionId}/report")
    public ResponseEntity<ReplayReport> getReplayReportBySessionId(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId) {
        return ResponseEntity.ok(forensicQueryService.getReplayReportBySessionId(sessionId));
    }

    @Operation(summary = "Verify session hash",
               description = "Re-computes the session hash and compares to the provided value. " +
                             "Returns true if the session is untampered.")
    @GetMapping("/sessions/{sessionId}/verify")
    public ResponseEntity<Boolean> verifyReplayHash(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId,
            @Parameter(description = "Expected SHA-256 hash") @RequestParam String hash) {
        return ResponseEntity.ok(forensicQueryService.verifyReplayHash(sessionId, hash));
    }

    @Operation(summary = "Verify individual event hashes",
               description = "Re-computes canonical SHA-256 for every event in a session. " +
                             "Returns only tampered events — empty list means all clean (PRD AC-08).")
    @GetMapping("/sessions/{sessionId}/verify-hashes")
    public ResponseEntity<List<VerificationResult>> verifyEventHashes(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId) {
        return ResponseEntity.ok(forensicQueryService.verifyEventHashes(sessionId));
    }
}
