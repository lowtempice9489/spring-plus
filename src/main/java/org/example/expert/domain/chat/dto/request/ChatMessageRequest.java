package org.example.expert.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {

    @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
    @Size(max = 20, message = "닉네임은 20자를 초과할 수 없습니다.")
    private String nickname;

    @NotBlank(message = "채팅 메시지는 비어 있을 수 없습니다.")
    @Size(max = 75, message = "채팅 내용이 너무 깁니다.")
    private String content;
}