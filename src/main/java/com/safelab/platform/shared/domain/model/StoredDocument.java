package com.safelab.platform.shared.domain.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stored_documents", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stored_documents_collection", columnNames = "collection_name")
})
public class StoredDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "collection_name", nullable = false, length = 120)
    private String collectionName;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StoredDocument() {}

    public StoredDocument(String collectionName, String content) {
        this.collectionName = collectionName;
        this.content = content;
    }

    @PrePersist
    public void onCreate() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
