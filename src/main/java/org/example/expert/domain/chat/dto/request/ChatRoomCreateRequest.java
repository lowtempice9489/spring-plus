package org.example.expert.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomCreateRequest {

    @NotBlank(message = "채팅방 이름은 비어있을 수 없습니다.")
    @Size(max = 10, message = "채팅방 이름이 너무 깁니다.")
    private String name;
}