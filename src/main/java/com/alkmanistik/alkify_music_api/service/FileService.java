package com.alkmanistik.alkify_music_api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

    public String uploadFile(String path, MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is null or empty");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Original filename is invalid");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID() + extension;

        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Path filePath = dirPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File uploaded to: {}", filePath);

        return fileName;
    }

    public InputStream getResourceFile(String path, String fileName) throws FileNotFoundException {

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Filename is null or empty");
        }

        Path filePath = Paths.get(path).resolve(fileName);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        return new FileInputStream(filePath.toFile());
    }


    public void deleteFile(String path, String fileName) {
        Path fullPath = Paths.get(path, fileName).normalize();
        try {
            boolean deleted = Files.deleteIfExists(fullPath);
            if (deleted) {
                log.info("File deleted: {}", fullPath);
            } else {
                log.warn("File not found for deletion: {}", fullPath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fullPath, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
