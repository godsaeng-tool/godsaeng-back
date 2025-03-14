package com.example.godsaengbackend.repository;

import com.example.godsaengbackend.entity.ChatSession;
import com.example.godsaengbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Page<ChatSession> findByUser(User user, Pageable pageable);
    Page<ChatSession> findByUserAndLecture_Id(User user, Long lectureId, Pageable pageable);
    Optional<ChatSession> findByIdAndUser(Long id, User user);
} 