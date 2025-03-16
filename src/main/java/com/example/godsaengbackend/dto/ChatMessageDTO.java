package com.example.godsaengbackend.dto;

import com.example.godsaengbackend.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long lectureId;
    private String content;
    private String role;  // "user" 또는 "assistant"
    private LocalDateTime timestamp;
    private Long parentId;  // 답변의 경우 질문 ID

    public static ChatMessageDTO fromEntity(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .lectureId(message.getLecture().getId())
                .content(message.getContent())
                .role(message.getRole().toString().toLowerCase())
                .timestamp(message.getCreatedAt())
                .parentId(message.getParentId())
                .build();
    }
} 