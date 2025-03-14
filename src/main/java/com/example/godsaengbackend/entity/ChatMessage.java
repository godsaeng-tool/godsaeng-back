package com.example.godsaengbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id", nullable = false)
    private ChatSession chatSession;
    
    @Enumerated(EnumType.STRING)
    private MessageRole role;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    public enum MessageRole {
        USER, ASSISTANT
    }
} 