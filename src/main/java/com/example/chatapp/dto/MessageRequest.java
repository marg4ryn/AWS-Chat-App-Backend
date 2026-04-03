package com.example.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageRequest {

    @NotBlank(message = "Username cannot be empty")
    private String senderUsername;

    private String content;

    private Long mediaId;
}
