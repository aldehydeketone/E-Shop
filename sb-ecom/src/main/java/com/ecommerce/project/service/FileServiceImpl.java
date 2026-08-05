package com.ecommerce.project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new IOException("Uploaded image must have a file extension");
        }

        // === DIAGNOSTIC LOGGING (RENDER EVIDENCE) ===
        Path directory = Paths.get(path);
        File dirFile = directory.toAbsolutePath().toFile();
        log.info("[DIAG] project.image raw value            : {}", path);
        log.info("[DIAG] Absolute directory path            : {}", dirFile.getAbsolutePath());
        log.info("[DIAG] Directory exists before mkdirs     : {}", dirFile.exists());
        log.info("[DIAG] JVM working directory (user.dir)   : {}", System.getProperty("user.dir"));

        Files.createDirectories(directory);
        boolean existsAfter = dirFile.exists();
        boolean canWrite    = dirFile.canWrite();
        log.info("[DIAG] Directory exists after mkdirs      : {}", existsAfter);
        log.info("[DIAG] Directory canWrite                 : {}", canWrite);
        // ============================================

        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        Path destPath = directory.resolve(fileName);

        log.info("[DIAG] Destination file path              : {}", destPath.toAbsolutePath());

        Files.copy(file.getInputStream(), destPath);

        boolean savedExists = destPath.toFile().exists();
        log.info("[DIAG] Destination file exists after copy : {}", savedExists);

        return fileName;
    }
}
