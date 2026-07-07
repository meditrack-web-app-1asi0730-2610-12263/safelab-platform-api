package com.safelab.platform.alerts.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.BusinessEventService;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/alerts-notifications")
@Tag(name = "8. Alerts & Notifications")
public class AlertsController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public AlertsController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @GetMapping("/alerts")
    public Object alerts() { return collections.get("alerts"); }

    @GetMapping("/notifications")
    public Object notifications() { return collections.get("notifications"); }

    @PostMapping("/alerts/{id}/acknowledge")
    public ObjectNode acknowledge(@PathVariable String id, @RequestBody(required = false) ObjectNode body) {
        var patch = collections.object();
        patch.put("status", "acknowledged");
        patch.put("acknowledgedAt", Instant.now().toString());
        var alert = collections.patch("alerts", id, patch);
        String actor = body != null && body.hasNonNull("actor") ? body.get("actor").asText() : collections.text(alert, "assignedTo");
        events.audit(actor, "Alert acknowledged: " + collections.text(alert, "title"), "Alerts & Notifications", collections.text(alert, "severity"), id);
        events.notify("Alert acknowledged", collections.text(alert, "title"), "alerts", "info");
        return alert;
    }

    @PostMapping("/alerts/{id}/resolve")
    public ObjectNode resolve(@PathVariable String id, @RequestBody(required = false) ObjectNode body) {
        var patch = collections.object();
        patch.put("status", "resolved");
        patch.put("resolvedAt", Instant.now().toString());
        var alert = collections.patch("alerts", id, patch);
        String actor = body != null && body.hasNonNull("actor") ? body.get("actor").asText() : collections.text(alert, "assignedTo");
        events.audit(actor, "Alert resolved: " + collections.text(alert, "title"), "Alerts & Notifications", collections.text(alert, "severity"), id);
        events.notify("Alert resolved", collections.text(alert, "title"), "alerts", "success");
        return alert;
    }

    @PostMapping("/alerts/{id}/escalate")
    public ObjectNode escalate(@PathVariable String id, @RequestBody(required = false) ObjectNode body) {
        var alert = collections.findById("alerts", id);
        var patch = collections.object();
        patch.put("status", "escalated");
        collections.patch("alerts", id, patch);
        String actor = body != null && body.hasNonNull("actor") ? body.get("actor").asText() : collections.text(alert, "assignedTo");
        events.audit(actor, "Alert escalated: " + collections.text(alert, "title"), "Alerts & Notifications", collections.text(alert, "severity"), id);
        return events.createIncidentFromAlert(alert, actor);
    }

    @PostMapping("/notifications/{id}/mark-read")
    public ObjectNode markNotificationRead(@PathVariable String id) {
        var patch = collections.object();
        patch.put("unread", false);
        return collections.patch("notifications", id, patch);
    }
}
