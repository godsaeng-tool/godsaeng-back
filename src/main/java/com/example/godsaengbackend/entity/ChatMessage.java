package com.example.godsaengbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 사용자 메시지인 경우에만 설정됨

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageRole role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 질문-답변 쌍을 연결하기 위한 필드
    @Column(name = "parent_id")
    private Long parentId;  // 답변의 경우 질문 메시지의 ID를 저장

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum MessageRole {
        USER, ASSISTANT
    }
} 