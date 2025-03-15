package com.example.godsaengbackend.controller;

import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.service.AIService;
import com.example.godsaengbackend.service.LectureService;
import com.example.godsaengbackend.service.StudyService;
import com.example.godsaengbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/callback")
public class AICallbackController {

    private static final Logger logger = LoggerFactory.getLogger(AICallbackController.class);
    
    private final LectureService lectureService;
    private final StudyService studyService;
    private final AIService aiService;

    public AICallbackController(LectureService lectureService, StudyService studyService, AIService aiService) {
        this.lectureService = lectureService;
        this.studyService = studyService;
        this.aiService = aiService;
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
        try {
            // 요청 데이터 로깅
            logger.info("콜백 요청 데이터: {}", request);
            
            Long lectureId = Long.valueOf(request.get("lecture_id").toString());
            
            // AI 서버의 결과 JSON 형식에 맞게 데이터 추출
            String transcript = (String) request.get("transcribed_text");
            String summary = (String) request.get("summary_text");
            String expectedQuestions = (String) request.get("quiz_text");
            
            logger.info("강의 ID {}의 처리가 완료되었습니다.", lectureId);
            lectureService.updateLectureResult(lectureId, transcript, summary, expectedQuestions);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("콜백 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/embedding")
    public ResponseEntity<?> updateEmbedding(@RequestBody Map<String, Object> request) {
        Long lectureId = Long.valueOf(request.get("lecture_id").toString());
        String vectorDbId = (String) request.get("vector_db_id");
        
        logger.info("강의 ID {}의 임베딩이 완료되었습니다. Vector DB ID: {}", lectureId, vectorDbId);
        lectureService.updateEmbeddingStatus(lectureId, vectorDbId);
        
        return ResponseEntity.ok().build();
    }
    
    // AI가 자동으로 학습 계획을 생성하여 콜백으로 전달하는 엔드포인트
    @PostMapping("/study-plan")
    public ResponseEntity<?> createStudyPlan(@RequestBody Map<String, Object> request) {
        Long lectureId = Long.valueOf(request.get("lecture_id").toString());
        String userEmail = (String) request.get("email");
        String planDetails = (String) request.get("plan_details");
        
        logger.info("강의 ID {}에 대한 학습 계획이 AI에서 생성되었습니다.", lectureId);
        studyService.saveAIGeneratedStudyPlan(userEmail, lectureId, planDetails);
        
        return ResponseEntity.ok().build();
    }

    // 결과 파일 직접 조회 엔드포인트 추가 (콜백이 실패할 경우 대비)
    @GetMapping("/result/{taskId}")
    public ResponseEntity<?> getTaskResult(@PathVariable String taskId) {
        try {
            Map<String, Object> result = aiService.getLectureResult(taskId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("결과 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
} 