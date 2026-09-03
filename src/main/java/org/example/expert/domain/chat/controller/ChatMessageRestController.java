package org.example.expert.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.chat.dto.response.ChatMessageResponse;
import org.example.expert.domain.chat.service.ChatMessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms")
public class ChatMessageRestController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/{roomId}/messages")
    public List<ChatMessageResponse> getRecentMessages(
            @PathVariable Long roomId
    ) {
        return chatMessageService
                .getRecentRoomMessages(roomId);
    }
}