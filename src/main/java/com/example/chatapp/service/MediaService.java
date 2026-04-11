package com.example.chatapp.service;

import com.example.chatapp.dto.MediaResponse;
import com.example.chatapp.entity.MediaFile;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final S3Client s3Client;
    private final S3Presigner presigner;

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

    public ResponseEntity<String> getMediaUrl(Long id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "File not found"));

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(mediaFile.getFilePath())
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(30))
                    .getObjectRequest(getObjectRequest)
                    .build();

            String url = presigner.presignGetObject(presignRequest).url().toString();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(url))
                    .build();

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Presign error");
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