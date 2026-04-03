package com.example.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaResponse {
    private Long id;
    private String filename;
    private String contentType;
    private String downloadUrl;
    private LocalDateTime uploadedAt;
}
