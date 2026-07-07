package com.safelab.platform.iam.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
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
@RequestMapping("/api/v1/auth")
@Tag(name = "1. Identity & Access Management")
public class AuthController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public AuthController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @PostMapping("/sign-in")
    public ObjectNode signIn(@RequestBody ObjectNode credentials) {
        String login = credentials.hasNonNull("email") ? credentials.get("email").asText() : credentials.path("username").asText();
        String password = credentials.path("password").asText();
        var user = collections.list("users").stream()
                .filter(candidate -> login.equalsIgnoreCase(collections.text(candidate, "email")) || login.equalsIgnoreCase(collections.text(candidate, "username")))
                .filter(candidate -> password.equals(collections.text(candidate, "password")))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        collections.save("currentUser", user);
        events.audit(collections.text(user, "fullName"), "User authenticated", "Identity & Access Management", "info", "SESSION");
        var response = collections.object();
        response.put("accessToken", "demo-" + UUID.randomUUID());
        response.put("tokenType", "Bearer");
        response.put("authenticatedAt", Instant.now().toString());
        response.set("user", user);
        return response;
    }

    @PostMapping("/sign-out")
    public ObjectNode signOut() {
        JsonNode user = collections.get("currentUser");
        events.audit(collections.text(user, "fullName"), "User logged out", "Identity & Access Management", "info", "SESSION");
        var guest = collections.object();
        guest.put("id", "guest");
        guest.put("fullName", "Guest");
        guest.put("role", "guest");
        guest.put("position", "Guest");
        guest.put("facilityId", "none");
        guest.set("allowedContexts", collections.array());
        collections.save("currentUser", guest);
        return guest;
    }

    @GetMapping("/me")
    public JsonNode me() {
        return collections.get("currentUser");
    }
}
