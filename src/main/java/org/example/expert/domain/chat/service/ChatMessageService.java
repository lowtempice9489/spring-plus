package org.example.expert.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.chat.dto.request.ChatMessageRequest;
import org.example.expert.domain.chat.dto.response.ChatMessageResponse;
import org.example.expert.domain.chat.entity.ChatMessage;
import org.example.expert.domain.chat.entity.ChatRoom;
import org.example.expert.domain.chat.repository.ChatMessageRepository;
import org.example.expert.domain.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatMessageResponse saveRoomMessage(Long roomId, ChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 채팅방입니다.")
                );

        LocalDateTime sentAt = LocalDateTime.now();

        ChatMessage chatMessage = new ChatMessage(
                chatRoom,
                request.getNickname().trim(),
                request.getContent().trim(),
                sentAt
        );

        chatMessageRepository.save(chatMessage);

        return new ChatMessageResponse(
                chatMessage.getNickname(),
                chatMessage.getContent(),
                chatMessage.getSentAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getRecentRoomMessages(Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new IllegalArgumentException(
                    "존재하지 않는 채팅방입니다."
            );
        }

        LocalDateTime oneHourAgo =
                LocalDateTime.now().minusHours(1);

        List<ChatMessage> messages =
                chatMessageRepository
                        .findTop10ByChatRoomIdAndSentAtAfterOrderBySentAtDesc(
                                roomId,
                                oneHourAgo
                        );

        return messages.stream()
                .sorted(
                        Comparator.comparing(
                                ChatMessage::getSentAt
                        )
                )
                .map(ChatMessageResponse::from)
                .toList();
    }
}
