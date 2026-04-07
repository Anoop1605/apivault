package com.sentinel.dashboard.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final RestTemplate restTemplate;
    private final String forensicsApiBaseUrl;

    public DashboardController(@Value("${forensics.api.base-url}") String forensicsApiBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.forensicsApiBaseUrl = forensicsApiBaseUrl;
    }

    private HttpEntity<?> getAuthenticatedRequest() {
        HttpHeaders headers = new HttpHeaders();
        String auth = "admin:admin";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return new HttpEntity<>(headers);
    }

    /**
     * GET / — Redirect to dashboard home
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    /**
     * GET /dashboard — Main Dashboard Overview Page
     */
    @GetMapping(value = { "", "/home" })
    public String dashboard(Model model) {
        try {
            // Mock stats data
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalRequests", "1,284,092");
            stats.put("totalRequestsChange", "+12%");
            stats.put("allowedTraffic", "94.2%");
            stats.put("deniedRequests", "48,203");
            stats.put("deniedChange", "+5.2%");
            stats.put("highRiskSessions", "142");

            // Mock top endpoints data
            List<Map<String, Object>> topEndpoints = new ArrayList<>();
            topEndpoints.add(createEndpoint("/api/v1/auth/login", "42.1k", 85));
            topEndpoints.add(createEndpoint("/api/v1/user/profile", "38.4k", 70));
            topEndpoints.add(createEndpoint("/api/v1/transactions", "22.9k", 45));
            topEndpoints.add(createEndpoint("/healthz", "15.2k", 30));

            // Mock flagged sessions data
            List<Map<String, Object>> flaggedSessions = new ArrayList<>();
            flaggedSessions.add(createSession("sess_8f2a...", "192.168.1.104", "CRITICAL", 98, "Blocked", "2m ago"));
            flaggedSessions.add(createSession("sess_4c1e...", "45.23.11.90", "HIGH", 74, "Challenged", "14m ago"));
            flaggedSessions.add(createSession("sess_9a2b...", "103.4.201.2", "MEDIUM", 52, "Allowed", "28m ago"));

            model.addAttribute("pageTitle", "Security Overview - Obsidian Sec");
            model.addAttribute("currentPage", "dashboard");
            model.addAttribute("stats", stats);
            model.addAttribute("topEndpoints", topEndpoints);
            model.addAttribute("flaggedSessions", flaggedSessions);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
        }
        return "dashboard";
    }

    /**
     * GET /dashboard/home — Alias for dashboard
     * Home — list of all sessions.
     */
    @GetMapping("/sessions")
    public String home(Model model) {
        try {
            List<?> sessions = restTemplate.exchange(
                    forensicsApiBaseUrl + "/forensics/query/sessions",
                    org.springframework.http.HttpMethod.GET,
                    getAuthenticatedRequest(),
                    List.class).getBody();
            sessions = sessions != null ? sessions : Collections.emptyList();

            // Calculate total events
            int totalEvents = 0;
            if (!sessions.isEmpty()) {
                for (Object session : sessions) {
                    if (session instanceof Map) {
                        Map<?, ?> sessionMap = (Map<?, ?>) session;
                        Object eventCount = sessionMap.get("eventCount");
                        if (eventCount instanceof Integer) {
                            totalEvents += (Integer) eventCount;
                        } else if (eventCount instanceof Long) {
                            totalEvents += ((Long) eventCount).intValue();
                        } else if (eventCount instanceof Number) {
                            totalEvents += ((Number) eventCount).intValue();
                        }
                    }
                }
            }

            model.addAttribute("sessions", sessions);
            model.addAttribute("totalEvents", totalEvents);
        } catch (Exception e) {
            model.addAttribute("sessions", Collections.emptyList());
            model.addAttribute("totalEvents", 0);
            model.addAttribute("error", "Could not reach forensics API: " + e.getMessage());
        }
        return "session-explorer";
    }

    /**
     * GET /dashboard/timeline — Event Timeline Page
     */
    @GetMapping("/timeline")
    public String timelinePage(Model model) {
        model.addAttribute("currentPage", "timeline");
        model.addAttribute("pageTitle", "Event Timeline - NYXSEC");
        List<Map<String, Object>> events = new ArrayList<>();
        events.add(createEvent("14:22:11", "Auth Bypass", "/api/admin", "DENY", 98));
        events.add(createEvent("14:20:02", "Standard Login", "/auth/login", "ALLOW", 12));
        events.add(createEvent("14:18:55", "SQL Injection", "/api/search", "DENY", 100));
        model.addAttribute("events", events);
        return "timeline";
    }

    /**
     * GET /dashboard/session-explorer — Session Explorer Page
     */
    @GetMapping("/session-explorer")
    public String sessionExplorer(Model model) {
        model.addAttribute("currentPage", "session-explorer");
        model.addAttribute("pageTitle", "Session Explorer - NYXSEC");
        List<Map<String, Object>> sessions = new ArrayList<>();
        sessions.add(createSessionData("SES-8921-X9", "192.168.1.45", 88, 142, 12, true));
        sessions.add(createSessionData("SES-4412-B2", "192.168.1.50", 32, 56, 0, false));
        sessions.add(createSessionData("SES-0091-R4", "192.168.1.60", 12, 12, 0, false));
        model.addAttribute("sessions", sessions);
        return "session-explorer";
    }

    /**
     * GET /dashboard/policy-trace — Policy Trace Analysis Page
     */
    @GetMapping("/policy-trace")
    public String policyTrace(Model model) {
        model.addAttribute("currentPage", "policy-trace");
        model.addAttribute("pageTitle", "Policy Trace - NYXSEC");
        Map<String, Object> traceData = new LinkedHashMap<>();
        traceData.put("outcome", "DENY");
        traceData.put("ruleId", "POL-9928-XR");
        traceData.put("subject", "Marcus Holloway");
        model.addAttribute("trace", traceData);
        return "policy-trace";
    }

    /**
     * GET /dashboard/replay — Replay Engine Page
     */
    @GetMapping("/replay")
    public String replay(Model model) {
        model.addAttribute("currentPage", "replay");
        model.addAttribute("pageTitle", "Replay Engine - NYXSEC");
        Map<String, Object> replayData = new LinkedHashMap<>();
        replayData.put("sessionId", "SID-8829-X-2026");
        replayData.put("duration", "00:14:22");
        replayData.put("events", 1024);
        model.addAttribute("replay", replayData);
        return "replay";
    }

    /**
     * GET /dashboard/whatif — What-If Simulation Page
     */
    @GetMapping("/whatif")
    public String whatif(Model model) {
        model.addAttribute("currentPage", "whatif");
        model.addAttribute("pageTitle", "What-If Simulation - NYXSEC");
        Map<String, Object> simulation = new LinkedHashMap<>();
        simulation.put("policy", "v2.4 Patch");
        simulation.put("divergenceCount", 1);
        model.addAttribute("simulation", simulation);
        return "whatif";
    }

    /**
     * GET /dashboard/alerts — Security Alerts Page
     */
    @GetMapping("/alerts")
    public String alerts(Model model) {
        model.addAttribute("currentPage", "alerts");
        model.addAttribute("pageTitle", "Security Alerts - NYXSEC");
        List<Map<String, Object>> alerts = new ArrayList<>();
        alerts.add(createAlert("SQL Injection", "SES-8921-XF", 0.88, "2m ago"));
        alerts.add(createAlert("Privilege Escalation", "SES-4410-QR", 0.74, "14m ago"));
        alerts.add(createAlert("Anomalous Behavior", "SES-2291-ZZ", 0.71, "32m ago"));
        model.addAttribute("alerts", alerts);
        return "alerts";
    }

    /**
     * GET /dashboard/metrics — System Metrics Page
     */
    @GetMapping("/metrics")
    public String metrics(Model model) {
        model.addAttribute("currentPage", "metrics");
        model.addAttribute("pageTitle", "System Metrics - NYXSEC");
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("p99Latency", "142ms");
        metrics.put("eventWriteCount", "2.4M/hr");
        metrics.put("errorRate", "0.02%");
        model.addAttribute("metrics", metrics);
        return "metrics";
    }

    /**
     * GET /dashboard/sessions/{sessionId}/timeline
     * Event timeline for a session.
     */
    @GetMapping("/sessions/{sessionId}/timeline")
    public String sessionTimeline(@PathVariable String sessionId, Model model) {
        model.addAttribute("currentPage", "timeline");
        model.addAttribute("pageTitle", "Session Timeline - NYXSEC");
        model.addAttribute("sessionId", sessionId);
        try {
            List<?> timeline = restTemplate.exchange(
                    forensicsApiBaseUrl + "/forensics/dashboard/sessions/" + sessionId + "/timeline",
                    org.springframework.http.HttpMethod.GET,
                    getAuthenticatedRequest(),
                    List.class).getBody();
            model.addAttribute("timeline", timeline != null ? timeline : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("timeline", Collections.emptyList());
            model.addAttribute("error", "Could not load timeline: " + e.getMessage());
        }
        return "timeline";
    }

    // Helper methods
    private Map<String, Object> createEvent(String time, String type, String endpoint, String decision, int risk) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("time", time);
        event.put("type", type);
        event.put("endpoint", endpoint);
        event.put("decision", decision);
        event.put("risk", risk);
        return event;
    }

    private Map<String, Object> createSessionData(String id, String ip, int maxRisk, int eventCount, int denyCount, boolean flagged) {
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("id", id);
        session.put("ip", ip);
        session.put("maxRisk", maxRisk);
        session.put("eventCount", eventCount);
        session.put("denyCount", denyCount);
        session.put("flagged", flagged);
        return session;
    }

    private Map<String, Object> createAlert(String type, String sessionId, double riskScore, String time) {
        Map<String, Object> alert = new LinkedHashMap<>();
        alert.put("type", type);
        alert.put("sessionId", sessionId);
        alert.put("riskScore", riskScore);
        alert.put("time", time);
        return alert;
    }

    /**
     * GET /dashboard/sessions/{sessionId}/timeline
     * Event timeline for a session.
     */
    @GetMapping("/sessions/{sessionId}/policy-trace")
    public String policyTrace(@PathVariable UUID sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        try {
            Map<?, ?> trace = restTemplate.exchange(
                    forensicsApiBaseUrl + "/forensics/dashboard/sessions/" + sessionId + "/policy-trace",
                    org.springframework.http.HttpMethod.GET,
                    getAuthenticatedRequest(),
                    Map.class).getBody();
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
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, getAuthenticatedRequest().getHeaders());
            Map<?, ?> report = restTemplate.exchange(
                    forensicsApiBaseUrl + "/forensics/sessions/" + sessionId + "/whatif",
                    org.springframework.http.HttpMethod.POST,
                    requestEntity,
                    Map.class).getBody();
            model.addAttribute("report", report != null ? report : Collections.emptyMap());
        } catch (Exception e) {
            model.addAttribute("report", Collections.emptyMap());
            model.addAttribute("error", "Simulation failed: " + e.getMessage());
        }
        return "whatif";
    }

    // Helper methods for mock data
    private Map<String, Object> createEndpoint(String name, String count, int percentage) {
        Map<String, Object> endpoint = new LinkedHashMap<>();
        endpoint.put("name", name);
        endpoint.put("count", count);
        endpoint.put("percentage", percentage);
        return endpoint;
    }

    private Map<String, Object> createSession(String id, String ip, String riskLevel, int riskScore, String action, String time) {
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("id", id);
        session.put("ip", ip);
        session.put("riskLevel", riskLevel);
        session.put("riskScore", riskScore);
        session.put("action", action);
        session.put("time", time);
        return session;
    }
}
