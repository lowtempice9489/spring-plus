package org.example.expert.domain.chat.dto.response;

import lombok.Getter;
import org.example.expert.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponse {

    private final String nickname;
    private final String content;
    private final LocalDateTime sentAt;

    public ChatMessageResponse(String nickname, String content, LocalDateTime sentAt) {
        this.nickname = nickname;
        this.content = content;
        this.sentAt = sentAt;
    }

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getNickname(),
                chatMessage.getContent(),
                chatMessage.getSentAt()
        );
    }
}