package com.safelab.platform.shared.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.domain.model.StoredDocument;
import com.safelab.platform.shared.infrastructure.persistence.StoredDocumentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class JsonCollectionService {
    private final StoredDocumentRepository repository;
    private final ObjectMapper mapper;

    public JsonCollectionService(StoredDocumentRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public ObjectMapper mapper() { return mapper; }

    @Transactional(readOnly = true)
    public JsonNode get(String collectionName) {
        var document = repository.findByCollectionName(collectionName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found: " + collectionName));
        try {
            return mapper.readTree(document.getContent());
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid stored JSON for collection: " + collectionName, ex);
        }
    }

    @Transactional
    public JsonNode save(String collectionName, JsonNode content) {
        try {
            var serialized = mapper.writeValueAsString(content);
            var document = repository.findByCollectionName(collectionName)
                    .orElseGet(() -> new StoredDocument(collectionName, serialized));
            document.setContent(serialized);
            repository.save(document);
            return content;
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not serialize collection: " + collectionName, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<ObjectNode> list(String collectionName) {
        JsonNode node = get(collectionName);
        if (!node.isArray()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, collectionName + " is not a list collection");
        }
        List<ObjectNode> items = new ArrayList<>();
        node.forEach(item -> {
            if (item instanceof ObjectNode objectNode) items.add(objectNode);
        });
        return items;
    }

    @Transactional(readOnly = true)
    public ObjectNode findById(String collectionName, String id) {
        return list(collectionName).stream()
                .filter(item -> id.equals(text(item, "id")))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, collectionName + " item not found: " + id));
    }

    @Transactional
    public ObjectNode create(String collectionName, ObjectNode item) {
        ArrayNode array = mutableArray(collectionName);
        if (!item.hasNonNull("id") || item.get("id").asText().isBlank()) {
            item.put("id", generateId(collectionName));
        }
        if (!item.has("createdAt")) item.put("createdAt", Instant.now().toString());
        array.add(item);
        save(collectionName, array);
        return item;
    }

    @Transactional
    public ObjectNode replace(String collectionName, String id, ObjectNode replacement) {
        ArrayNode array = mutableArray(collectionName);
        for (int i = 0; i < array.size(); i++) {
            var current = array.get(i);
            if (current instanceof ObjectNode objectNode && id.equals(text(objectNode, "id"))) {
                replacement.put("id", id);
                if (!replacement.has("updatedAt")) replacement.put("updatedAt", Instant.now().toString());
                array.set(i, replacement);
                save(collectionName, array);
                return replacement;
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, collectionName + " item not found: " + id);
    }

    @Transactional
    public ObjectNode patch(String collectionName, String id, ObjectNode patch) {
        ArrayNode array = mutableArray(collectionName);
        for (int i = 0; i < array.size(); i++) {
            var current = array.get(i);
            if (current instanceof ObjectNode objectNode && id.equals(text(objectNode, "id"))) {
                patch.fields().forEachRemaining(entry -> objectNode.set(entry.getKey(), entry.getValue()));
                objectNode.put("updatedAt", Instant.now().toString());
                array.set(i, objectNode);
                save(collectionName, array);
                return objectNode;
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, collectionName + " item not found: " + id);
    }

    @Transactional
    public void delete(String collectionName, String id) {
        ArrayNode array = mutableArray(collectionName);
        ArrayNode result = mapper.createArrayNode();
        boolean removed = false;
        for (JsonNode item : array) {
            if (item instanceof ObjectNode objectNode && id.equals(text(objectNode, "id"))) {
                removed = true;
            } else {
                result.add(item);
            }
        }
        if (!removed) throw new ResponseStatusException(HttpStatus.NOT_FOUND, collectionName + " item not found: " + id);
        save(collectionName, result);
    }

    @Transactional
    public void append(String collectionName, ObjectNode item) {
        ArrayNode array = mutableArray(collectionName);
        if (!item.hasNonNull("id") || item.get("id").asText().isBlank()) {
            item.put("id", generateId(collectionName));
        }
        array.add(item);
        save(collectionName, array);
    }

    @Transactional
    public void resetFromSeed(JsonNode seedRoot) {
        repository.deleteAll();
        seedRoot.fields().forEachRemaining(entry -> save(entry.getKey(), entry.getValue()));
    }

    public ObjectNode object() { return mapper.createObjectNode(); }
    public ArrayNode array() { return mapper.createArrayNode(); }

    public String text(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) ? node.get(field).asText() : "";
    }

    public double number(JsonNode node, String field, double fallback) {
        return node != null && node.hasNonNull(field) && node.get(field).isNumber() ? node.get(field).asDouble() : fallback;
    }

    public int integer(JsonNode node, String field, int fallback) {
        return node != null && node.hasNonNull(field) && node.get(field).isNumber() ? node.get(field).asInt() : fallback;
    }

    public boolean bool(JsonNode node, String field, boolean fallback) {
        return node != null && node.hasNonNull(field) && node.get(field).isBoolean() ? node.get(field).asBoolean() : fallback;
    }

    private ArrayNode mutableArray(String collectionName) {
        JsonNode node = get(collectionName);
        if (!node.isArray()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, collectionName + " is not a list collection");
        ArrayNode copy = mapper.createArrayNode();
        node.forEach(copy::add);
        return copy;
    }

    private String generateId(String collectionName) {
        String prefix = switch (collectionName) {
            case "users" -> "usr";
            case "facilities" -> "fac";
            case "sensors" -> "sen";
            case "assets" -> "asset";
            case "complianceRules" -> "rule";
            case "alerts" -> "alert";
            case "notifications" -> "not";
            case "incidents" -> "inc";
            case "actuators" -> "act";
            case "remoteCommands" -> "cmd";
            case "reports" -> "rep";
            case "auditLogs" -> "aud";
            default -> collectionName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
        };
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
