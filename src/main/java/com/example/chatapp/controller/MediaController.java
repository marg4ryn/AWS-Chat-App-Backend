package com.example.chatapp.controller;

import com.example.chatapp.dto.MediaResponse;
import com.example.chatapp.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadMedia(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.uploadMedia(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getMedia(@PathVariable Long id) {
        return mediaService.getMediaUrl(id);
    }
}