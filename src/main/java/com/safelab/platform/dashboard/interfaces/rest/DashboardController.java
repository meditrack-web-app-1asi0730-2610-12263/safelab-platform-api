package com.safelab.platform.dashboard.interfaces.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.BusinessEventService;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "4. Dashboard & Overview")
public class DashboardController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public DashboardController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @GetMapping("/overview")
    public ObjectNode overview(@RequestParam(required = false, defaultValue = "global") String facilityId) {
        var sensors = filterByFacility("sensors", facilityId);
        var assets = filterByFacility("assets", facilityId);
        var alerts = filterByFacility("alerts", facilityId);
        var incidents = filterByFacility("incidents", facilityId);
        var reports = collections.list("reports");
        var facilities = "global".equals(facilityId) ? collections.list("facilities") : filterById("facilities", facilityId);

        long activeSensors = sensors.stream().filter(s -> "online".equalsIgnoreCase(collections.text(s, "connection"))).count();
        long outOfRange = sensors.stream().filter(s -> !"normal".equalsIgnoreCase(collections.text(s, "status"))).count();
        long activeAlerts = alerts.stream().filter(a -> "active".equalsIgnoreCase(collections.text(a, "status"))).count();
        long criticalIncidents = incidents.stream().filter(i -> "critical".equalsIgnoreCase(collections.text(i, "severity"))).count();
        long compliantAssets = assets.stream().filter(a -> "compliant".equalsIgnoreCase(collections.text(a, "status"))).count();
        int complianceScore = assets.isEmpty() ? 0 : (int)Math.round((compliantAssets * 100.0) / assets.size());
        int telemetryScore = sensors.isEmpty() ? 0 : (int)Math.round((activeSensors * 100.0) / sensors.size());
        int healthScore = (int)Math.max(0, Math.round((complianceScore * 0.45) + (telemetryScore * 0.35) - (activeAlerts * 4) - (criticalIncidents * 8)));

        var root = collections.object();
        root.put("facilityId", facilityId);
        root.put("healthScore", healthScore);
        root.put("complianceScore", complianceScore);
        root.put("telemetryScore", telemetryScore);
        root.put("activeSensors", activeSensors);
        root.put("totalSensors", sensors.size());
        root.put("storageUnits", assets.size());
        root.put("openAlerts", activeAlerts);
        root.put("criticalIncidents", criticalIncidents);
        root.put("reportsReady", reports.stream().filter(r -> "ready".equalsIgnoreCase(collections.text(r, "status"))).count());
        root.set("facilities", toArray(facilities));
        root.set("priorityAlerts", toArray(alerts.stream().limit(5).toList()));
        root.set("recentIncidents", toArray(incidents.stream().limit(5).toList()));
        root.set("notifications", collections.get("notifications"));
        return root;
    }

    @PostMapping("/refresh")
    public ObjectNode refresh(@RequestParam(required = false, defaultValue = "global") String facilityId) {
        events.audit("System", "Dashboard data refreshed", "Dashboard & Overview", "info", "DASHBOARD");
        return overview(facilityId);
    }

    private java.util.List<ObjectNode> filterByFacility(String collection, String facilityId) {
        var all = collections.list(collection);
        if (facilityId == null || facilityId.isBlank() || "global".equalsIgnoreCase(facilityId)) return all;
        return all.stream().filter(item -> facilityId.equalsIgnoreCase(collections.text(item, "facilityId"))).toList();
    }

    private java.util.List<ObjectNode> filterById(String collection, String id) {
        return collections.list(collection).stream().filter(item -> id.equalsIgnoreCase(collections.text(item, "id"))).toList();
    }

    private ArrayNode toArray(java.util.List<ObjectNode> values) {
        var array = collections.array();
        values.forEach(array::add);
        return array;
    }
}
