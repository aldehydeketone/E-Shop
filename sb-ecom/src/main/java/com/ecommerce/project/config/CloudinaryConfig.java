package com.ecommerce.project.config;

import com.cloudinary.Cloudinary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

    @Value("${cloudinary.cloud_name:}")
    private String cloudName;

    @Value("${cloudinary.api_key:}")
    private String apiKey;

    @Value("${cloudinary.api_secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        String cleanCloudName = sanitize(cloudName);
        String cleanApiKey = sanitize(apiKey);
        String cleanApiSecret = sanitize(apiSecret);

        log.info("[CLOUDINARY CONFIG] Cloud Name: '{}'", cleanCloudName);
        log.info("[CLOUDINARY CONFIG] API Key: '{}'", cleanApiKey);
        log.info("[CLOUDINARY CONFIG] API Secret Length: {}", cleanApiSecret != null ? cleanApiSecret.length() : 0);

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cleanCloudName);
        config.put("api_key", cleanApiKey);
        config.put("api_secret", cleanApiSecret);

        return new Cloudinary(config);
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        if (trimmed.startsWith("'") && trimmed.endsWith("'")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed.trim();
    }
}
