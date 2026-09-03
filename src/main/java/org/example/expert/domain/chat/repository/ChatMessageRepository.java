package org.example.expert.domain.chat.repository;

import org.example.expert.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop10ByChatRoomIdAndSentAtAfterOrderBySentAtDesc(
            Long chatRoomId, LocalDateTime sentAt
    );
}