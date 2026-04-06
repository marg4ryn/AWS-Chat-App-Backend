package com.example.chatapp.service;

import com.example.chatapp.dto.MediaResponse;
import com.example.chatapp.entity.MediaFile;
import com.example.chatapp.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucketName;

    public MediaResponse uploadMedia(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String originalFilename = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename()));

        String key = UUID.randomUUID() + "_" + originalFilename;

        try (InputStream inputStream = file.getInputStream()) {

            s3Client.putObject(
                    b -> b.bucket(bucketName)
                          .key(key)
                          .contentType(file.getContentType()),
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                            inputStream, file.getSize())
            );

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "S3 upload error");
        }

        MediaFile mediaFile = new MediaFile();
        mediaFile.setFilename(originalFilename);
        mediaFile.setContentType(file.getContentType());
        mediaFile.setFilePath(key);

        MediaFile saved = mediaFileRepository.save(mediaFile);
        return toDto(saved);
    }

    public ResponseEntity<Resource> getMediaResource(Long id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "File not found"));

        try {
            var s3Object = s3Client.getObject(
                    b -> b.bucket(bucketName)
                          .key(mediaFile.getFilePath())
            );

            InputStream inputStream = s3Object;

            Resource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mediaFile.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + mediaFile.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "S3 read error");
        }
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