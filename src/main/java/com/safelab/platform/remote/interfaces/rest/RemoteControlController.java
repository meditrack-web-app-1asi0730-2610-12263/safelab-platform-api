package com.safelab.platform.remote.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.BusinessEventService;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/remote-control")
@Tag(name = "9. Remote Control & Actuation")
public class RemoteControlController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public RemoteControlController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @GetMapping("/actuators")
    public Object actuators() { return collections.get("actuators"); }

    @GetMapping("/commands")
    public Object commands() { return collections.get("remoteCommands"); }

    @PostMapping("/actuators/{id}/commands")
    public ObjectNode execute(@PathVariable String id, @RequestBody ObjectNode body) {
        var actuator = collections.findById("actuators", id);
        String command = body.hasNonNull("command") ? body.get("command").asText() : "Reset";
        String requestedBy = body.hasNonNull("requestedBy") ? body.get("requestedBy").asText() : "SafeLab Administrator";
        String safetyState = collections.text(actuator, "safetyState");
        String connection = collections.text(actuator, "status");

        if ("blocked".equalsIgnoreCase(safetyState) || "offline".equalsIgnoreCase(connection)) {
            events.audit(requestedBy, "Rejected remote command " + command + " for " + collections.text(actuator, "name"), "Remote Control", "warning", id);
            events.notify("Remote command rejected", collections.text(actuator, "name") + " cannot execute " + command + ".", "remote-control", "warning");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Actuator is blocked or offline");
        }

        boolean running = switch (command.toLowerCase()) {
            case "start" -> true;
            case "stop" -> false;
            case "reset" -> false;
            default -> collections.bool(actuator, "running", false);
        };
        var patch = collections.object();
        patch.put("running", running);
        patch.put("lastCommand", command);
        patch.put("updatedAt", Instant.now().toString());
        var updated = collections.patch("actuators", id, patch);

        var record = collections.object();
        record.put("id", "cmd-" + UUID.randomUUID().toString().substring(0, 8));
        record.put("command", command);
        record.put("device", collections.text(updated, "name"));
        record.put("actuatorId", id);
        record.put("status", "completed");
        record.put("requestedBy", requestedBy);
        record.put("createdAt", Instant.now().toString());
        collections.append("remoteCommands", record);

        events.audit(requestedBy, "Executed remote command " + command + " for " + collections.text(updated, "name"), "Remote Control", "info", record.get("id").asText());
        events.notify("Remote command executed", command + " executed for " + collections.text(updated, "name") + ".", "remote-control", "info");
        return record;
    }
}
