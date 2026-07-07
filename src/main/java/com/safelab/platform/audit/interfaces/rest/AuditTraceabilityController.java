package com.safelab.platform.audit.interfaces.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-traceability")
@Tag(name = "12. Audit & Traceability")
public class AuditTraceabilityController {
    private final JsonCollectionService collections;

    public AuditTraceabilityController(JsonCollectionService collections) {
        this.collections = collections;
    }

    @GetMapping("/audit-logs")
    public Object logs() { return collections.get("auditLogs"); }

    @GetMapping("/timeline")
    public ObjectNode timeline() {
        var result = collections.object();
        var events = collections.list("auditLogs");
        result.put("total", events.size());
        result.put("criticalEvents", events.stream().filter(e -> "critical".equalsIgnoreCase(collections.text(e, "severity"))).count());
        result.put("pendingReview", events.stream().filter(e -> "pending".equalsIgnoreCase(collections.text(e, "status"))).count());
        result.put("integrityScore", calculateIntegrityScore(events));
        result.set("events", toArray(events.stream().limit(20).toList()));
        return result;
    }

    @PostMapping("/audit-logs/{id}/mark-reviewed")
    public ObjectNode markReviewed(@PathVariable String id) {
        var patch = collections.object();
        patch.put("status", "reviewed");
        return collections.patch("auditLogs", id, patch);
    }

    private int calculateIntegrityScore(java.util.List<ObjectNode> events) {
        if (events.isEmpty()) return 100;
        long pending = events.stream().filter(e -> "pending".equalsIgnoreCase(collections.text(e, "status"))).count();
        return (int)Math.max(0, Math.round(((events.size() - pending) * 100.0) / events.size()));
    }

    private ArrayNode toArray(java.util.List<ObjectNode> values) {
        var array = collections.array();
        values.forEach(array::add);
        return array;
    }
}
