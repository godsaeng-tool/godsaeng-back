package com.example.godsaengbackend.dto;

import com.example.godsaengbackend.entity.ChatMessage;
import com.example.godsaengbackend.entity.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

public class ChatDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSessionRequest {
        @NotNull(message = "강의 ID는 필수 입력값입니다.")
        private Long lectureId;
        
        private String title;
        
        // 말투 설정 추가 (기본값은 NORMAL)
        private ChatSession.ToneType tone = ChatSession.ToneType.NORMAL;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionResponse {
        private Long id;
        private Long userId;
        private Long lectureId;
        private String title;
        private ChatSession.ToneType tone;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public static SessionResponse fromEntity(ChatSession session) {
            return SessionResponse.builder()
                    .id(session.getId())
                    .userId(session.getUser().getId())
                    .lectureId(session.getLecture().getId())
                    .title(session.getTitle())
                    .tone(session.getTone())
                    .createdAt(session.getCreatedAt())
                    .updatedAt(session.getUpdatedAt())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionDetailResponse {
        private Long id;
        private Long userId;
        private Long lectureId;
        private String title;
        private ChatSession.ToneType tone;
        private List<MessageResponse> messages;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public static SessionDetailResponse fromEntity(ChatSession session, List<ChatMessage> messages) {
            return SessionDetailResponse.builder()
                    .id(session.getId())
                    .userId(session.getUser().getId())
                    .lectureId(session.getLecture().getId())
                    .title(session.getTitle())
                    .tone(session.getTone())
                    .messages(messages.stream()
                            .map(MessageResponse::fromEntity)
                            .collect(Collectors.toList()))
                    .createdAt(session.getCreatedAt())
                    .updatedAt(session.getUpdatedAt())
                    .build();
        }
    }
    
    // 말투 정보 조회용 DTO 추가
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToneOption {
        private String code;
        private String description;
        
        public static List<ToneOption> getAllTones() {
            return Arrays.stream(ChatSession.ToneType.values())
                    .map(tone -> new ToneOption(tone.name(), tone.getDescription()))
                    .collect(Collectors.toList());
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageRequest {
        @NotBlank(message = "메시지 내용은 필수 입력값입니다.")
        private String content;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private Long id;
        private String role;
        private String content;
        private LocalDateTime createdAt;
        
        public static MessageResponse fromEntity(ChatMessage message) {
            return MessageResponse.builder()
                    .id(message.getId())
                    .role(message.getRole().toString())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .build();
        }
    }
} 