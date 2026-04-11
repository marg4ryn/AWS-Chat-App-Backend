package com.example.chatapp.controller;

import com.example.chatapp.dto.*;
import com.example.chatapp.entity.User;
import com.example.chatapp.service.MessageService;
import com.example.chatapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        User sender = userService.getOrCreate(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(request, sender));
    }
}