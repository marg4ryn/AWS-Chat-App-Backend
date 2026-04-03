package com.example.chatapp.service;

import com.example.chatapp.dto.*;
import com.example.chatapp.entity.*;
import com.example.chatapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final UserRepository userRepository;

    @Value("${app.media.upload-dir}")
    private String uploadDir;

    public MediaResponse uploadMedia(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file is empty");
        }

        // We create a directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create file directory");
        }

        // We generate a unique file name
        String originalFilename = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename()));
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path targetPath = uploadPath.resolve(storedFilename);

        try {
            Files.copy(file.getInputStream(), targetPath,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "File saving error");
        }

        MediaFile mediaFile = new MediaFile();
        mediaFile.setFilename(originalFilename);
        mediaFile.setContentType(file.getContentType());
        mediaFile.setFilePath(targetPath.toString());

        MediaFile saved = mediaFileRepository.save(mediaFile);
        return toDto(saved);
    }

    public ResponseEntity<Resource> getMediaResource(Long id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The file does not exist"));

        Path filePath = Paths.get(mediaFile.getFilePath());
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The file does not exist on the disk");
            }
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Incorrect file path");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaFile.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + mediaFile.getFilename() + "\"")
                .body(resource);
    }

    public MediaResponse toDto(MediaFile mediaFile) {
        return MediaResponse.builder()
                .id(mediaFile.getId())
                .filename(mediaFile.getFilename())
                .contentType(mediaFile.getContentType())
                .downloadUrl("/api/media/" + mediaFile.getId())
                .uploadedAt(mediaFile.getUploadedAt())
                .build();
    }
}
