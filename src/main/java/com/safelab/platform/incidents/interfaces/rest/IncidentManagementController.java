package com.safelab.platform.incidents.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.BusinessEventService;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/incident-management")
@Tag(name = "11. Incident Management")
public class IncidentManagementController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public IncidentManagementController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @GetMapping("/incidents")
    public Object incidents() { return collections.get("incidents"); }

    @PostMapping("/incidents")
    public ObjectNode create(@RequestBody ObjectNode body) {
        if (!body.has("status")) body.put("status", "open");
        if (!body.has("createdAt")) body.put("createdAt", Instant.now().toString());
        if (!body.has("updatedAt")) body.put("updatedAt", Instant.now().toString());
        var incident = collections.create("incidents", body);
        events.audit(collections.text(incident, "assignedTo"), "Incident registered: " + collections.text(incident, "title"), "Incident Management", collections.text(incident, "severity"), collections.text(incident, "id"));
        events.notify("Incident registered", collections.text(incident, "title"), "incidents", collections.text(incident, "severity"));
        return incident;
    }

    @PostMapping("/incidents/{id}/start-investigation")
    public ObjectNode startInvestigation(@PathVariable String id) { return changeStatus(id, "investigating", "Started investigation"); }

    @PostMapping("/incidents/{id}/mark-resolved")
    public ObjectNode markResolved(@PathVariable String id) { return changeStatus(id, "resolved", "Marked incident as resolved"); }

    @PostMapping("/incidents/{id}/close")
    public ObjectNode close(@PathVariable String id) { return changeStatus(id, "closed", "Closed incident"); }

    @GetMapping("/incidents/{id}/report")
    public ObjectNode report(@PathVariable String id) {
        var incident = collections.findById("incidents", id);
        var report = collections.object();
        report.put("id", "incident-report-" + id);
        report.put("title", collections.text(incident, "title"));
        report.put("summary", collections.text(incident, "description"));
        report.put("status", collections.text(incident, "status"));
        report.put("severity", collections.text(incident, "severity"));
        report.put("generatedAt", Instant.now().toString());
        report.set("incident", incident);
        return report;
    }

    private ObjectNode changeStatus(String id, String status, String action) {
        var patch = collections.object();
        patch.put("status", status);
        patch.put("updatedAt", Instant.now().toString());
        var incident = collections.patch("incidents", id, patch);
        events.audit(collections.text(incident, "assignedTo"), action + ": " + collections.text(incident, "title"), "Incident Management", collections.text(incident, "severity"), id);
        events.notify(action, collections.text(incident, "title"), "incidents", collections.text(incident, "severity"));
        return incident;
    }
}
