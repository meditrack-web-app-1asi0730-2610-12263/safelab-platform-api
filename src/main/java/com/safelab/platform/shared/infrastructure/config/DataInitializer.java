package com.safelab.platform.shared.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safelab.platform.shared.application.JsonCollectionService;
import com.safelab.platform.shared.infrastructure.persistence.StoredDocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final StoredDocumentRepository repository;
    private final JsonCollectionService collections;
    private final ObjectMapper mapper;
    private final boolean resetOnStart;

    public DataInitializer(StoredDocumentRepository repository,
                           JsonCollectionService collections,
                           ObjectMapper mapper,
                           @Value("${safelab.seed.reset-on-start:false}") boolean resetOnStart) {
        this.repository = repository;
        this.collections = collections;
        this.mapper = mapper;
        this.resetOnStart = resetOnStart;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0 && !resetOnStart) return;
        var resource = new ClassPathResource("db.seed.json");
        try (var input = resource.getInputStream()) {
            var seed = mapper.readTree(input);
            collections.resetFromSeed(seed);
        }
    }
}
