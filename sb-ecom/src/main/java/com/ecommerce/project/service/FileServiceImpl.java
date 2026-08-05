package com.ecommerce.project.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Cannot upload an empty file");
        }

        log.info("[CLOUDINARY] Uploading file '{}' (size: {} bytes) to Cloudinary...",
                file.getOriginalFilename(), file.getSize());

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String secureUrl = (String) uploadResult.get("secure_url");

        log.info("[CLOUDINARY] Successfully uploaded. Secure URL: {}", secureUrl);
        return secureUrl;
    }

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        return uploadImage(file);
    }

    @Override
    public boolean deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            log.info("[CLOUDINARY] Skipping delete: URL '{}' is null or not a Cloudinary asset", imageUrl);
            return false;
        }

        try {
            String publicId = extractPublicId(imageUrl);
            log.info("[CLOUDINARY] Deleting asset with publicId: '{}' (from URL: '{}')", publicId, imageUrl);

            Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String result = (String) deleteResult.get("result");
            log.info("[CLOUDINARY] Delete result for publicId '{}': {}", publicId, result);
            return "ok".equalsIgnoreCase(result);
        } catch (Exception e) {
            log.error("[CLOUDINARY] Failed to delete image from Cloudinary: {}", e.getMessage(), e);
            return false;
        }
    }

    private String extractPublicId(String imageUrl) {
        int uploadIndex = imageUrl.indexOf("/upload/");
        if (uploadIndex == -1) {
            return "";
        }
        String pathAfterUpload = imageUrl.substring(uploadIndex + "/upload/".length());

        if (pathAfterUpload.matches("^v\\d+/.*")) {
            pathAfterUpload = pathAfterUpload.substring(pathAfterUpload.indexOf('/') + 1);
        }

        int lastDotIndex = pathAfterUpload.lastIndexOf('.');
        if (lastDotIndex != -1) {
            pathAfterUpload = pathAfterUpload.substring(0, lastDotIndex);
        }
        return pathAfterUpload;
    }
}
