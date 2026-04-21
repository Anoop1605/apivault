package com.sentinel.dashboard.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final RestTemplate restTemplate;
    private final String forensicsApiBaseUrl;

    public DashboardController(@Value("${forensics.api.base-url}") String forensicsApiBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.forensicsApiBaseUrl = forensicsApiBaseUrl;
    }

    /**
     * GET /dashboard
     * Home — list of all sessions.
     */
    @GetMapping
    public String home(Model model) {
        try {
            List<?> sessions = restTemplate.getForObject(
                    forensicsApiBaseUrl + "/forensics/query/sessions", List.class);
            model.addAttribute("sessions", sessions != null ? sessions : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("sessions", Collections.emptyList());
            model.addAttribute("error", "Could not reach forensics API: " + e.getMessage());
        }
        return "session-explorer";
    }

    /**
     * GET /dashboard/sessions/{sessionId}/timeline
     * Chronological event timeline for a session.
     */
    @GetMapping("/sessions/{sessionId}/timeline")
    public String timeline(@PathVariable UUID sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        try {
            List<?> timeline = restTemplate.getForObject(
                    forensicsApiBaseUrl + "/forensics/dashboard/sessions/" + sessionId + "/timeline",
                    List.class);
            model.addAttribute("timeline", timeline != null ? timeline : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("timeline", Collections.emptyList());
            model.addAttribute("error", "Could not load timeline: " + e.getMessage());
        }
        return "timeline";
    }

    /**
     * GET /dashboard/sessions/{sessionId}/policy-trace
     * Policy decision trace for a session.
     */
    @GetMapping("/sessions/{sessionId}/policy-trace")
    public String policyTrace(@PathVariable UUID sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        try {
            Map<?, ?> trace = restTemplate.getForObject(
                    forensicsApiBaseUrl + "/forensics/dashboard/sessions/" + sessionId + "/policy-trace",
                    Map.class);
            model.addAttribute("trace", trace != null ? trace : Collections.emptyMap());
        } catch (Exception e) {
            model.addAttribute("trace", Collections.emptyMap());
            model.addAttribute("error", "Could not load policy trace: " + e.getMessage());
        }
        return "policy-trace";
    }

    /**
     * GET /dashboard/sessions/{sessionId}/whatif
     * What-if simulation launcher for a session.
     */
    @GetMapping("/sessions/{sessionId}/whatif")
    public String whatif(@PathVariable UUID sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        return "whatif";
    }

    /**
     * POST /dashboard/sessions/{sessionId}/whatif
     * Run what-if simulation with provided alternate rules.
     */
    @PostMapping("/sessions/{sessionId}/whatif")
    public String runWhatIf(@PathVariable UUID sessionId,
                             @RequestParam String rules,
                             Model model) {
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("rules", rules);
        try {
            Map<String, Object> requestBody = Map.of("rules", List.of(rules.split(",")));
            Map<?, ?> report = restTemplate.postForObject(
                    forensicsApiBaseUrl + "/forensics/sessions/" + sessionId + "/whatif",
                    requestBody, Map.class);
            model.addAttribute("report", report != null ? report : Collections.emptyMap());
        } catch (Exception e) {
            model.addAttribute("report", Collections.emptyMap());
            model.addAttribute("error", "Simulation failed: " + e.getMessage());
        }
        return "whatif";
    }
}
