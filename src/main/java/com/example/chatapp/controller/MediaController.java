package com.example.chatapp.controller;

import com.example.chatapp.dto.MediaResponse;
import com.example.chatapp.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MediaController {

    private final MediaService mediaService;

    // POST upload a media file
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadMedia(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.uploadMedia(file));
    }

    // GET download media file by ID
    @GetMapping("/{id}")
    public ResponseEntity<String> getMedia(@PathVariable Long id) {
        return mediaService.getMediaUrl(id);
    }
}
