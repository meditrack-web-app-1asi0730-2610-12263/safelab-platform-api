package com.safelab.platform.shared.infrastructure.persistence;

import com.safelab.platform.shared.domain.model.StoredDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoredDocumentRepository extends JpaRepository<StoredDocument, Long> {
    Optional<StoredDocument> findByCollectionName(String collectionName);
    boolean existsByCollectionName(String collectionName);
}
