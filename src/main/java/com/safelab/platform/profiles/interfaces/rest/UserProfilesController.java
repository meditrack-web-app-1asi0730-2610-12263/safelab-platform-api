package com.safelab.platform.profiles.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.BusinessEventService;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-profiles")
@Tag(name = "2. User Profiles")
public class UserProfilesController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public UserProfilesController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @GetMapping("/users")
    public Object users() { return collections.get("users"); }

    @GetMapping("/users/{id}")
    public ObjectNode user(@PathVariable String id) { return collections.findById("users", id); }

    @PatchMapping("/users/{id}")
    public ObjectNode update(@PathVariable String id, @RequestBody ObjectNode body) {
        var user = collections.patch("users", id, body);
        events.audit(collections.text(user, "fullName"), "User profile updated", "User Profiles", "info", id);
        return user;
    }
}
