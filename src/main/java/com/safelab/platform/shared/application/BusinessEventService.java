package com.safelab.platform.shared.application;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class BusinessEventService {
    private final JsonCollectionService collections;

    public BusinessEventService(JsonCollectionService collections) {
        this.collections = collections;
    }

    @Transactional
    public ObjectNode notify(String title, String message, String context, String severity) {
        var notification = collections.object();
        notification.put("id", "not-" + UUID.randomUUID().toString().substring(0, 8));
        notification.put("title", title);
        notification.put("message", message);
        notification.put("context", context);
        notification.put("severity", severity == null ? "info" : severity.toLowerCase(Locale.ROOT));
        notification.put("unread", true);
        notification.put("createdAt", Instant.now().toString());
        collections.append("notifications", notification);
        return notification;
    }

    @Transactional
    public ObjectNode audit(String actor, String action, String module, String severity, String evidence) {
        var audit = collections.object();
        audit.put("id", "aud-" + UUID.randomUUID().toString().substring(0, 8));
        audit.put("actor", actor == null || actor.isBlank() ? "System" : actor);
        audit.put("action", action);
        audit.put("module", module);
        audit.put("severity", severity == null ? "info" : severity.toLowerCase(Locale.ROOT));
        audit.put("status", "reviewed");
        audit.put("createdAt", Instant.now().toString());
        audit.put("evidence", evidence == null || evidence.isBlank() ? "EVD-" + System.currentTimeMillis() % 10000 : evidence);
        collections.append("auditLogs", audit);
        return audit;
    }

    @Transactional
    public ObjectNode createAlert(String type, String title, String message, String sensorId, String assetId,
                                  String facilityId, String severity, String assignedTo) {
        var alert = collections.object();
        alert.put("id", "alert-" + UUID.randomUUID().toString().substring(0, 8));
        alert.put("type", type);
        alert.put("title", title);
        alert.put("message", message);
        alert.put("sensorId", sensorId);
        alert.put("assetId", assetId == null ? "" : assetId);
        alert.put("facilityId", facilityId == null ? "" : facilityId);
        alert.put("severity", severity == null ? "warning" : severity.toLowerCase(Locale.ROOT));
        alert.put("status", "active");
        alert.put("createdAt", Instant.now().toString());
        alert.put("assignedTo", assignedTo == null || assignedTo.isBlank() ? "SafeLab Administrator" : assignedTo);
        collections.append("alerts", alert);
        notify(title, message, "alerts", severity);
        audit("System", "Created alert " + title, "Alerts & Notifications", severity, alert.get("id").asText());
        return alert;
    }

    @Transactional
    public ObjectNode createIncidentFromAlert(ObjectNode alert, String assignedTo) {
        var incident = collections.object();
        incident.put("id", "inc-" + UUID.randomUUID().toString().substring(0, 8));
        incident.put("code", "INC-" + (int) (System.currentTimeMillis() % 100000));
        incident.put("title", collections.text(alert, "title"));
        incident.put("description", collections.text(alert, "message"));
        incident.put("facilityId", collections.text(alert, "facilityId"));
        incident.put("relatedAlertId", collections.text(alert, "id"));
        incident.put("relatedSensorId", collections.text(alert, "sensorId"));
        incident.put("relatedAssetId", collections.text(alert, "assetId"));
        incident.put("severity", collections.text(alert, "severity"));
        incident.put("status", "open");
        incident.put("assignedTo", assignedTo == null || assignedTo.isBlank() ? collections.text(alert, "assignedTo") : assignedTo);
        incident.put("dueDate", Instant.now().plusSeconds(86400).toString().substring(0, 10));
        incident.put("evidence", 0);
        incident.put("createdAt", Instant.now().toString());
        incident.put("updatedAt", Instant.now().toString());
        collections.append("incidents", incident);
        notify("Incident opened", collections.text(alert, "title"), "incidents", collections.text(alert, "severity"));
        audit("System", "Opened incident from alert " + collections.text(alert, "title"), "Incident Management", collections.text(alert, "severity"), incident.get("id").asText());
        return incident;
    }
}
