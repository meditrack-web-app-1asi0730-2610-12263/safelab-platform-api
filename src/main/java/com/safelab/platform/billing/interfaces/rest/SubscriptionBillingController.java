package com.safelab.platform.billing.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.BusinessEventService;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscription-billing")
@Tag(name = "3. Subscription & Billing")
public class SubscriptionBillingController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public SubscriptionBillingController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @GetMapping("/billing")
    public JsonNode billing() { return collections.get("billing"); }

    @PatchMapping("/billing")
    public JsonNode updateBilling(@RequestBody ObjectNode body) {
        var current = collections.get("billing").deepCopy();
        if (current instanceof ObjectNode objectNode) {
            body.fields().forEachRemaining(entry -> objectNode.set(entry.getKey(), entry.getValue()));
            collections.save("billing", objectNode);
            events.audit("SafeLab Administrator", "Billing information updated", "Subscription & Billing", "info", "BILLING");
            return objectNode;
        }
        return current;
    }
}
