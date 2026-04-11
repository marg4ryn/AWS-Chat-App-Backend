package com.example.chatapp.service;

import com.example.chatapp.dto.*;
import com.example.chatapp.entity.*;
import com.example.chatapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MediaFileRepository mediaFileRepository;
    private final MediaService mediaService;

    @Transactional(readOnly = true)
    public List<MessageResponse> getAllMessages() {
        return messageRepository.findAllByOrderBySentAtAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public MessageResponse sendMessage(MessageRequest request, User sender) {
        if (request.getContent() == null && request.getMediaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The message must contain content or an attachment");
        }

        MediaFile media = null;
        if (request.getMediaId() != null) {
            media = mediaFileRepository.findById(request.getMediaId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "The media file does not exist"));
        }

        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(sender);
        message.setMedia(media);

        return toDto(messageRepository.save(message));
    }

    private MessageResponse toDto(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .media(message.getMedia() != null
                        ? mediaService.toDto(message.getMedia())
                        : null)
                .build();
    }
}