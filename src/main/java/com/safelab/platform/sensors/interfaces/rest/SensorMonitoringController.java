package com.safelab.platform.sensors.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.BusinessEventService;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/sensor-monitoring")
@Tag(name = "6. Sensor Monitoring")
public class SensorMonitoringController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public SensorMonitoringController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @GetMapping("/sensors")
    public Object sensors() { return collections.get("sensors"); }

    @PostMapping("/sensors")
    public ObjectNode register(@RequestBody ObjectNode body) {
        if (!body.has("connection")) body.put("connection", "online");
        if (!body.has("status")) body.put("status", "normal");
        if (!body.has("lastReading")) body.put("lastReading", Instant.now().toString());
        var sensor = collections.create("sensors", body);
        events.audit(collections.text(sensor, "responsible"), "Sensor registered: " + collections.text(sensor, "name"), "Sensor Monitoring", "info", collections.text(sensor, "id"));
        events.notify("Sensor registered", collections.text(sensor, "name") + " is now available for monitoring.", "sensors", "info");
        return sensor;
    }

    @PatchMapping("/sensors/{id}/reading")
    public ObjectNode recordReading(@PathVariable String id, @RequestBody ObjectNode body) {
        var sensor = collections.findById("sensors", id);
        double value = body.hasNonNull("value") ? body.get("value").asDouble() : collections.number(sensor, "value", 0);
        double min = collections.number(sensor, "min", Double.NEGATIVE_INFINITY);
        double max = collections.number(sensor, "max", Double.POSITIVE_INFINITY);
        String status = (value < min || value > max) ? "out-of-range" : "normal";
        var patch = collections.object();
        patch.put("value", value);
        patch.put("status", status);
        patch.put("connection", "online");
        patch.put("lastReading", Instant.now().toString());
        var updated = collections.patch("sensors", id, patch);
        events.audit(collections.text(updated, "responsible"), "Sensor reading recorded: " + collections.text(updated, "name"), "Sensor Monitoring", "info", id);
        if ("out-of-range".equals(status)) {
            String type = collections.text(updated, "type");
            String severity = "temperature".equalsIgnoreCase(type) ? "critical" : "warning";
            events.createAlert(type, collections.text(updated, "name") + " out of range",
                    "Current value " + value + collections.text(updated, "unit") + " is outside allowed range.",
                    id, collections.text(updated, "assetId"), collections.text(updated, "facilityId"), severity,
                    collections.text(updated, "responsible"));
        }
        return updated;
    }

    @PostMapping("/sensors/{id}/disconnect")
    public ObjectNode disconnect(@PathVariable String id) {
        var patch = collections.object();
        patch.put("connection", "offline");
        patch.put("status", "invalid");
        patch.put("lastReading", Instant.now().toString());
        var updated = collections.patch("sensors", id, patch);
        events.createAlert("Connectivity", collections.text(updated, "name") + " disconnected",
                "Sensor stopped sending readings.", id, collections.text(updated, "assetId"), collections.text(updated, "facilityId"), "warning", collections.text(updated, "responsible"));
        return updated;
    }
}
