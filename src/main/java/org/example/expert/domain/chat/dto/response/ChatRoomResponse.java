package org.example.expert.domain.chat.dto.response;

import lombok.Getter;
import org.example.expert.domain.chat.entity.ChatRoom;

import java.time.LocalDateTime;

@Getter
public class ChatRoomResponse {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;

    public ChatRoomResponse(
            Long id,
            String name,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getName(),
                chatRoom.getCreatedAt()
        );
    }
}