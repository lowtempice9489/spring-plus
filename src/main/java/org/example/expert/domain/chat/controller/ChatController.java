package org.example.expert.domain.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.chat.dto.request.ChatMessageRequest;
import org.example.expert.domain.chat.dto.response.ChatMessageResponse;
import org.example.expert.domain.chat.service.ChatMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/global")
    public void sendGlobalMessage(
            @Valid ChatMessageRequest request
    ) {
        ChatMessageResponse response = new ChatMessageResponse(
                request.getNickname().trim(),
                request.getContent().trim(),
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/global",
                response
        );
    }

    @MessageMapping("/chat/rooms/{roomId}")
    public void sendRoomMessage(
            @DestinationVariable Long roomId,
            @Valid ChatMessageRequest request
    ) {
        ChatMessageResponse response =
                chatMessageService.saveRoomMessage(
                        roomId,
                        request
                );

        messagingTemplate.convertAndSend(
                "/topic/chat/rooms/" + roomId,
                response
        );
    }
}