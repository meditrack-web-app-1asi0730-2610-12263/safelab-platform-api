package com.safelab.platform.shared.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Generic JSON Collections", description = "Generic CRUD endpoints for frontend migration and early integration")
public class GenericCollectionController {
    private static final Set<String> ALLOWED_COLLECTIONS = Set.of(
            "users", "facilities", "sensors", "assets", "complianceRules", "alerts", "notifications",
            "incidents", "actuators", "remoteCommands", "reports", "auditLogs"
    );

    private final JsonCollectionService collections;

    public GenericCollectionController(JsonCollectionService collections) {
        this.collections = collections;
    }

    @GetMapping("/{collection}")
    public JsonNode list(@PathVariable String collection) {
        validate(collection);
        return collections.get(collection);
    }

    @GetMapping("/{collection}/{id}")
    public ObjectNode findById(@PathVariable String collection, @PathVariable String id) {
        validate(collection);
        return collections.findById(collection, id);
    }

    @PostMapping("/{collection}")
    public ObjectNode create(@PathVariable String collection, @RequestBody ObjectNode body) {
        validate(collection);
        return collections.create(collection, body);
    }

    @PutMapping("/{collection}/{id}")
    public ObjectNode replace(@PathVariable String collection, @PathVariable String id, @RequestBody ObjectNode body) {
        validate(collection);
        return collections.replace(collection, id, body);
    }

    @PatchMapping("/{collection}/{id}")
    public ObjectNode patch(@PathVariable String collection, @PathVariable String id, @RequestBody ObjectNode body) {
        validate(collection);
        return collections.patch(collection, id, body);
    }

    @DeleteMapping("/{collection}/{id}")
    public ResponseEntity<Void> delete(@PathVariable String collection, @PathVariable String id) {
        validate(collection);
        collections.delete(collection, id);
        return ResponseEntity.noContent().build();
    }

    private void validate(String collection) {
        if (!ALLOWED_COLLECTIONS.contains(collection)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Collection not exposed: " + collection);
        }
    }
}
