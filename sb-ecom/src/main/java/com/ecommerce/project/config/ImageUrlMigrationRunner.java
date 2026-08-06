package com.ecommerce.project.config;

import com.ecommerce.project.model.Product;
import com.ecommerce.project.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs at startup to fix image URLs that were incorrectly stored with a
 * double-prefix like:
 *   https://sb-ecom-latest-qurh.onrender.com/images/https://res.cloudinary.com/...
 *
 * Strips the local prefix so only the clean Cloudinary URL remains.
 * Safe to run multiple times (idempotent).
 */
@Component
public class ImageUrlMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImageUrlMigrationRunner.class);

    @Autowired
    private ProductRepository productRepository;

    @Value("${image.base.url:}")
    private String imageBaseUrl;

    @Override
    public void run(String... args) {
        log.info("[MIGRATION] Starting image URL migration. imageBaseUrl='{}'", imageBaseUrl);

        if (imageBaseUrl == null || imageBaseUrl.isBlank()) {
            log.info("[MIGRATION] imageBaseUrl is blank — skipping migration.");
            return;
        }

        // Normalize: ensure no trailing slash
        String prefix = imageBaseUrl.endsWith("/") ? imageBaseUrl : imageBaseUrl + "/";

        List<Product> products = productRepository.findAll();
        int fixed = 0;

        for (Product product : products) {
            String image = product.getImage();
            if (image == null) continue;

            // Fix pattern: <imageBaseUrl>/https://... -> https://...
            // e.g. "https://sb-ecom.onrender.com/images/https://res.cloudinary.com/..."
            if (image.startsWith(prefix + "https://") || image.startsWith(prefix + "http://")) {
                String cleanUrl = image.substring(prefix.length());
                log.info("[MIGRATION] Fixing product id={} name='{}': '{}' -> '{}'",
                        product.getProductId(), product.getProductName(),
                        image.substring(0, Math.min(image.length(), 80)) + "...",
                        cleanUrl.substring(0, Math.min(cleanUrl.length(), 80)) + "...");
                product.setImage(cleanUrl);
                productRepository.save(product);
                fixed++;
            }
        }

        log.info("[MIGRATION] Done. Fixed {} / {} product image URLs.", fixed, products.size());
    }
}
