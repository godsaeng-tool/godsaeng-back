package com.example.godsaengbackend.controller;

import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.service.LectureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/callback")
public class AICallbackController {

    private static final Logger logger = LoggerFactory.getLogger(AICallbackController.class);
    
    private final LectureService lectureService;

    public AICallbackController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @PostMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestBody Map<String, Object> request) {
        Long lectureId = Long.valueOf(request.get("lecture_id").toString());
        String statusStr = (String) request.get("status");
        Lecture.LectureStatus status = Lecture.LectureStatus.valueOf(statusStr);
        
        logger.info("강의 ID {}의 상태를 {}로 업데이트합니다.", lectureId, status);
        lectureService.updateLectureStatus(lectureId, status);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeProcessing(@RequestBody Map<String, Object> request) {
        Long lectureId = Long.valueOf(request.get("lecture_id").toString());
        String transcript = (String) request.get("transcript");
        String summary = (String) request.get("summary");
        String expectedQuestions = (String) request.get("expected_questions");
        
        logger.info("강의 ID {}의 처리가 완료되었습니다.", lectureId);
        lectureService.updateLectureResult(lectureId, transcript, summary, expectedQuestions);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/embedding")
    public ResponseEntity<?> updateEmbedding(@RequestBody Map<String, Object> request) {
        Long lectureId = Long.valueOf(request.get("lecture_id").toString());
        String vectorDbId = (String) request.get("vector_db_id");
        
        logger.info("강의 ID {}의 임베딩이 완료되었습니다. Vector DB ID: {}", lectureId, vectorDbId);
        lectureService.updateEmbeddingStatus(lectureId, vectorDbId);
        
        return ResponseEntity.ok().build();
    }
} 