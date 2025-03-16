package com.example.godsaengbackend.repository;

import com.example.godsaengbackend.entity.ChatMessage;
import com.example.godsaengbackend.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // 강의별 모든 메시지 조회 (생성 시간순)
    List<ChatMessage> findByLectureOrderByCreatedAtAsc(Lecture lecture);
    
    // 강의별 모든 메시지 조회 (ID 기준)
    List<ChatMessage> findByLectureIdOrderByIdAsc(Long lectureId);
    
    // 질문-답변 쌍으로 조회 (USER 역할 메시지와 그에 대응하는 ASSISTANT 메시지)
    @Query("SELECT m FROM ChatMessage m WHERE m.lecture.id = :lectureId AND " +
           "(m.role = com.example.godsaengbackend.entity.ChatMessage.MessageRole.USER OR " +
           "m.parentId IS NOT NULL) ORDER BY m.id ASC")
    List<ChatMessage> findQuestionAnswerPairsByLectureId(@Param("lectureId") Long lectureId);
    
    // 특정 질문에 대한 답변 조회
    ChatMessage findByParentId(Long parentId);
} 