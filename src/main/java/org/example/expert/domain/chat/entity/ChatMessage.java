package org.example.expert.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, length = 75)
    private String content;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    public ChatMessage(ChatRoom chatRoom, String nickname, String content, LocalDateTime sentAt) {
        this.chatRoom = chatRoom;
        this.nickname = nickname;
        this.content = content;
        this.sentAt = sentAt;
    }
}