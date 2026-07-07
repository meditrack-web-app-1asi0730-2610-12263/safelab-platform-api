package com.safelab.platform.reports.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.BusinessEventService;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports-analytics")
@Tag(name = "10. Reports & Analytics")
public class ReportsAnalyticsController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public ReportsAnalyticsController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @GetMapping("/reports")
    public Object reports() { return collections.get("reports"); }

    @GetMapping("/analytics")
    public ObjectNode analytics() {
        var root = collections.object();
        var sensors = collections.list("sensors");
        var alerts = collections.list("alerts");
        var incidents = collections.list("incidents");
        double avgTemperature = sensors.stream().filter(s -> "temperature".equalsIgnoreCase(collections.text(s, "type"))).mapToDouble(s -> collections.number(s, "value", 0)).average().orElse(0);
        double avgHumidity = sensors.stream().filter(s -> "humidity".equalsIgnoreCase(collections.text(s, "type"))).mapToDouble(s -> collections.number(s, "value", 0)).average().orElse(0);
        root.put("reports", collections.list("reports").size());
        root.put("activeAlerts", alerts.stream().filter(a -> "active".equalsIgnoreCase(collections.text(a, "status"))).count());
        root.put("incidents", incidents.size());
        root.put("averageTemperature", Math.round(avgTemperature * 10.0) / 10.0);
        root.put("averageHumidity", Math.round(avgHumidity * 10.0) / 10.0);
        return root;
    }

    @PostMapping("/reports/generate")
    public ObjectNode generate(@RequestBody(required = false) ObjectNode body) {
        String type = body != null && body.hasNonNull("type") ? body.get("type").asText() : "Operational";
        String format = body != null && body.hasNonNull("format") ? body.get("format").asText() : "PDF";
        var report = collections.object();
        report.put("id", "rep-" + UUID.randomUUID().toString().substring(0, 8));
        report.put("title", type + " Report " + Instant.now().toString().substring(0, 10));
        report.put("type", type);
        report.put("format", format);
        report.put("status", "ready");
        report.put("generatedAt", Instant.now().toString());
        collections.append("reports", report);
        events.audit("System", "Report generated: " + collections.text(report, "title"), "Reports & Analytics", "info", collections.text(report, "id"));
        events.notify("Report ready", collections.text(report, "title") + " is available for export.", "reports", "success");
        return report;
    }
}
