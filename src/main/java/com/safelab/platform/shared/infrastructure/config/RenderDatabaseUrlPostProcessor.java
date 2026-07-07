package com.safelab.platform.shared.infrastructure.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Render exposes PostgreSQL connection strings as postgres://user:pass@host:port/db.
 * Spring Boot expects jdbc:postgresql://host:port/db. This processor converts
 * Render's DATABASE_URL before the DataSource is created.
 */
public class RenderDatabaseUrlPostProcessor implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String explicitJdbcUrl = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("JDBC_DATABASE_URL")
        );
        String databaseUrl = environment.getProperty("DATABASE_URL");

        Map<String, Object> properties = new HashMap<>();

        if (explicitJdbcUrl != null) {
            properties.put("spring.datasource.url", explicitJdbcUrl);
        } else if (databaseUrl != null && !databaseUrl.isBlank()) {
            applyDatabaseUrl(databaseUrl.trim(), properties);
        }

        String explicitUsername = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_USERNAME"),
                environment.getProperty("JDBC_DATABASE_USERNAME"),
                environment.getProperty("DATABASE_USERNAME")
        );
        String explicitPassword = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_PASSWORD"),
                environment.getProperty("JDBC_DATABASE_PASSWORD"),
                environment.getProperty("DATABASE_PASSWORD")
        );

        if (explicitUsername != null) properties.put("spring.datasource.username", explicitUsername);
        if (explicitPassword != null) properties.put("spring.datasource.password", explicitPassword);

        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("renderDatabaseUrl", properties));
        }
    }

    private void applyDatabaseUrl(String databaseUrl, Map<String, Object> properties) {
        try {
            if (databaseUrl.startsWith("jdbc:")) {
                properties.put("spring.datasource.url", databaseUrl);
                return;
            }

            URI uri = URI.create(databaseUrl);
            String scheme = uri.getScheme();
            if (!"postgres".equalsIgnoreCase(scheme) && !"postgresql".equalsIgnoreCase(scheme)) {
                return;
            }

            String userInfo = uri.getRawUserInfo();
            if (userInfo != null) {
                String[] parts = userInfo.split(":", 2);
                if (parts.length > 0 && !parts[0].isBlank()) {
                    properties.put("spring.datasource.username", decode(parts[0]));
                }
                if (parts.length > 1) {
                    properties.put("spring.datasource.password", decode(parts[1]));
                }
            }

            String database = uri.getPath() == null ? "" : uri.getPath();
            String query = uri.getRawQuery();
            StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
                    .append(uri.getHost())
                    .append(uri.getPort() > 0 ? ":" + uri.getPort() : "")
                    .append(database);

            if (query != null && !query.isBlank()) {
                jdbc.append('?').append(query);
                if (!query.contains("sslmode=")) jdbc.append("&sslmode=require");
            } else {
                jdbc.append("?sslmode=require");
            }

            properties.put("spring.datasource.url", jdbc.toString());
            properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        } catch (Exception ignored) {
            // If parsing fails, Spring Boot will report the original datasource error.
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return null;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
