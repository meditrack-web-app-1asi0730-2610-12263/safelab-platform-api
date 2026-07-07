package com.safelab.platform.compliance.interfaces.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.BusinessEventService;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/environmental-compliance")
@Tag(name = "7. Environmental Compliance")
public class EnvironmentalComplianceController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public EnvironmentalComplianceController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @GetMapping("/rules")
    public Object rules() { return collections.get("complianceRules"); }

    @GetMapping("/status")
    public ObjectNode status() {
        var result = collections.object();
        var rules = collections.list("complianceRules");
        var sensors = collections.list("sensors");
        long violations = sensors.stream().filter(s -> !"normal".equalsIgnoreCase(collections.text(s, "status"))).count();
        int score = sensors.isEmpty() ? 0 : (int)Math.round(((sensors.size() - violations) * 100.0) / sensors.size());
        result.put("generalCompliance", score);
        result.put("monitoredSensors", sensors.size());
        result.put("activeViolations", violations);
        result.put("pendingAudits", rules.stream().filter(r -> collections.bool(r, "requiredEvidence", false)).count());
        result.set("rules", toArray(rules));
        result.set("violations", toArray(sensors.stream().filter(s -> !"normal".equalsIgnoreCase(collections.text(s, "status"))).toList()));
        return result;
    }

    @PostMapping("/evaluate")
    public ObjectNode evaluate() {
        var status = status();
        int violations = status.get("activeViolations").asInt();
        if (violations > 0) {
            events.audit("System", "Compliance evaluation detected " + violations + " violations", "Environmental Compliance", "warning", "COMPLIANCE");
            events.notify("Compliance violations detected", violations + " environmental rule deviations require review.", "compliance", "warning");
        }
        return status;
    }

    @PostMapping("/rules")
    public ObjectNode createRule(@RequestBody ObjectNode body) {
        var rule = collections.create("complianceRules", body);
        events.audit("Compliance Officer", "Compliance rule configured: " + collections.text(rule, "title"), "Environmental Compliance", "info", collections.text(rule, "id"));
        return rule;
    }

    private ArrayNode toArray(java.util.List<ObjectNode> values) {
        var array = collections.array();
        values.forEach(array::add);
        return array;
    }
}
