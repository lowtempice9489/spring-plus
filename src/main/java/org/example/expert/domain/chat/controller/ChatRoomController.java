package org.example.expert.domain.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.chat.dto.request.ChatRoomCreateRequest;
import org.example.expert.domain.chat.dto.response.ChatRoomResponse;
import org.example.expert.domain.chat.service.ChatRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatRoomResponse createRoom(
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        return chatRoomService.createRoom(request);
    }

    @GetMapping
    public List<ChatRoomResponse> getRooms() {
        return chatRoomService.getRooms();
    }
}