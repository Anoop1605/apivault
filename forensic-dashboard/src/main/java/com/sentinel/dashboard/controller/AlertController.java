package com.sentinel.dashboard.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/alerts")
public class AlertController {

    private final RestTemplate restTemplate;
    private final String forensicsApiBaseUrl;

    public AlertController(@Value("${forensics.api.base-url}") String forensicsApiBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.forensicsApiBaseUrl = forensicsApiBaseUrl;
    }

    /**
     * GET /dashboard/alerts
     * Alert list — all sessions that have BLOCK/REVIEW decisions.
     */
    @GetMapping
    public String listAlerts(Model model) {
        // Fetch known sessions then collect alerts for each
        try {
            List<?> sessions = restTemplate.getForObject(
                    forensicsApiBaseUrl + "/forensics/query/sessions", List.class);
            model.addAttribute("sessions", sessions != null ? sessions : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("sessions", Collections.emptyList());
            model.addAttribute("error", "Could not reach forensics API: " + e.getMessage());
        }
        return "alerts";
    }

    /**
     * GET /dashboard/alerts/{sessionId}
     * Alerts for a specific session.
     */
    @GetMapping("/{sessionId}")
    public String sessionAlerts(@PathVariable UUID sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        try {
            List<?> alerts = restTemplate.getForObject(
                    forensicsApiBaseUrl + "/forensics/dashboard/sessions/" + sessionId + "/alerts",
                    List.class);
            model.addAttribute("alerts", alerts != null ? alerts : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("alerts", Collections.emptyList());
            model.addAttribute("error", "Could not load alerts: " + e.getMessage());
        }
        return "alerts";
    }
}
