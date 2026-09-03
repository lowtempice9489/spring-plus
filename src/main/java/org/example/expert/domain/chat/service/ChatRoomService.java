package org.example.expert.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.chat.dto.request.ChatRoomCreateRequest;
import org.example.expert.domain.chat.dto.response.ChatRoomResponse;
import org.example.expert.domain.chat.entity.ChatRoom;
import org.example.expert.domain.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatRoomResponse createRoom(
            ChatRoomCreateRequest request
    ) {
        ChatRoom chatRoom = new ChatRoom(
                request.getName().trim()
        );

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomResponse.from(savedRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms() {
        return chatRoomRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ChatRoomResponse::from)
                .toList();
    }
}