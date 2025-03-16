package com.example.godsaengbackend.controller;

import com.example.godsaengbackend.dto.AICallbackDTO;
import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.service.AIService;
import com.example.godsaengbackend.service.LectureService;
import com.example.godsaengbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AICallbackController {

    private static final Logger logger = LoggerFactory.getLogger(AICallbackController.class);
    
    private final LectureService lectureService;
    private final AIService aiService;

    public AICallbackController(LectureService lectureService, AIService aiService) {
        this.lectureService = lectureService;
        this.aiService = aiService;
    }

    @PostMapping("/callback/complete")
    public ResponseEntity<?> handleAICallback(@RequestBody AICallbackDTO callbackData) {
        logger.info("AI 콜백 수신(complete): lecture_id={}, task_id={}", 
                   callbackData.getLecture_id(), callbackData.getTask_id());
        
        try {
            // AI 서버에서 받은 데이터로 강의 업데이트
            lectureService.updateLectureWithAIResult(callbackData);
            return ResponseEntity.ok().body(Map.of("status", "success"));
        } catch (Exception e) {
            logger.error("콜백 처리 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
} 