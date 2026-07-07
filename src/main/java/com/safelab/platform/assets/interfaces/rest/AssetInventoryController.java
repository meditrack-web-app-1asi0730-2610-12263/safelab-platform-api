package com.safelab.platform.assets.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.BusinessEventService;
import com.safelab.platform.shared.application.JsonCollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asset-inventory")
@Tag(name = "5. Asset & Inventory Monitoring")
public class AssetInventoryController {
    private final JsonCollectionService collections;
    private final BusinessEventService events;

    public AssetInventoryController(JsonCollectionService collections, BusinessEventService events) {
        this.collections = collections;
        this.events = events;
    }

    @GetMapping("/assets")
    public Object assets() { return collections.get("assets"); }

    @PostMapping("/assets")
    public ObjectNode create(@RequestBody ObjectNode body) {
        if (!body.has("status")) body.put("status", "compliant");
        var asset = collections.create("assets", body);
        events.audit(collections.text(asset, "responsible"), "Asset registered: " + collections.text(asset, "name"), "Asset & Inventory", "info", collections.text(asset, "id"));
        events.notify("Asset registered", collections.text(asset, "name") + " was added to inventory.", "assets", "info");
        return asset;
    }

    @PatchMapping("/assets/{id}")
    public ObjectNode update(@PathVariable String id, @RequestBody ObjectNode body) {
        var asset = collections.patch("assets", id, body);
        events.audit(collections.text(asset, "responsible"), "Asset updated: " + collections.text(asset, "name"), "Asset & Inventory", "info", id);
        return asset;
    }

    @DeleteMapping("/assets/{id}")
    public void delete(@PathVariable String id) {
        var asset = collections.findById("assets", id);
        collections.delete("assets", id);
        events.audit(collections.text(asset, "responsible"), "Asset deleted: " + collections.text(asset, "name"), "Asset & Inventory", "warning", id);
        events.notify("Asset deleted", collections.text(asset, "name") + " was removed from inventory.", "assets", "warning");
    }
}
