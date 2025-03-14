package com.example.godsaengbackend.repository;

import com.example.godsaengbackend.entity.ChatMessage;
import com.example.godsaengbackend.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByChatSession(ChatSession chatSession, Pageable pageable);
} 