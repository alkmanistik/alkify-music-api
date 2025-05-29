package com.alkmanistik.alkify_music_api.controller;

import com.alkmanistik.alkify_music_api.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Value("${project.images}")
    private String imagePath;

    @Value("${project.audios}")
    private String audioPath;

    @GetMapping("/audios/{audioName}")
    public void getAudio(@PathVariable String audioName, HttpServletResponse response) throws IOException {
        try (InputStream is = fileService.getResourceFile(audioPath, audioName)) {
            response.setContentType(MediaTypeFactory
                    .getMediaType(audioName)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM)
                    .toString());
            StreamUtils.copy(is, response.getOutputStream());
        }
    }

    @GetMapping("/images/{imageName}")
    public void getImage(@PathVariable String imageName, HttpServletResponse response) throws IOException {
        try (InputStream is = fileService.getResourceFile(imagePath, imageName)) {
            response.setContentType(MediaTypeFactory
                    .getMediaType(imageName)
                    .orElse(MediaType.IMAGE_PNG)  // Дефолтный тип для изображений
                    .toString());
            StreamUtils.copy(is, response.getOutputStream());
        }
    }

    @DeleteMapping("/audios/{audioName}")
    public ResponseEntity<String> deleteAudio(@PathVariable String audioName) throws IOException {
        fileService.deleteFile(audioPath, audioName);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/images/{imageName}")
    public ResponseEntity<String> deleteImage(@PathVariable String imageName) throws IOException {
        fileService.deleteFile(imagePath, imageName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/uploadAudio")
    public ResponseEntity<String> uploadAudio(@RequestPart MultipartFile file) throws IOException {
        validateAudioFile(file);
        String fileName = fileService.uploadFile(audioPath, file);
        return ResponseEntity.ok("Audio uploaded: " + fileName);
    }

    @PostMapping("/uploadImage")
    public ResponseEntity<String> uploadImage(@RequestPart MultipartFile file) throws IOException {
        validateImageFile(file);
        String fileName = fileService.uploadFile(imagePath, file);
        return ResponseEntity.ok("Image uploaded: " + fileName);
    }

    private void validateImageFile(MultipartFile file) {
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only images are allowed");
        }
    }

    private void validateAudioFile(MultipartFile file) {
        if (!file.getContentType().startsWith("audio/")) {
            throw new IllegalArgumentException("Only audio files are allowed");
        }
    }
}
