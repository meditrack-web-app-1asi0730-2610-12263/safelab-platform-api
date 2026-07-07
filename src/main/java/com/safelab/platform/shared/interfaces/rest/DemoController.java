package com.safelab.platform.shared.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/demo")
@Tag(name = "Demo Utilities")
public class DemoController {
    private final JsonCollectionService collections;
    private final ObjectMapper mapper;

    public DemoController(JsonCollectionService collections, ObjectMapper mapper) {
        this.collections = collections;
        this.mapper = mapper;
    }

    @PostMapping("/reset")
    public JsonNode reset() throws Exception {
        var resource = new ClassPathResource("db.seed.json");
        try (var input = resource.getInputStream()) {
            var seed = mapper.readTree(input);
            collections.resetFromSeed(seed);
            return seed;
        }
    }

    @GetMapping("/state")
    public JsonNode state() {
        var root = mapper.createObjectNode();
        for (String collection : java.util.List.of("users", "facilities", "sensors", "assets", "complianceRules", "alerts", "notifications", "incidents", "actuators", "remoteCommands", "reports", "auditLogs")) {
            root.set(collection, collections.get(collection));
        }
        root.set("billing", collections.get("billing"));
        root.set("currentUser", collections.get("currentUser"));
        return root;
    }
}
