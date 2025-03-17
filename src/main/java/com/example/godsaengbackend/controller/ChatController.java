package com.example.godsaengbackend.controller;

import com.example.godsaengbackend.dto.ChatHistoryDTO;
import com.example.godsaengbackend.dto.ChatRequestDTO;
import com.example.godsaengbackend.dto.ChatResponseDTO;
import com.example.godsaengbackend.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 강의에 대한 질문을 전송하고 AI 응답을 받습니다.
     */
    @PostMapping("/lectures/{lectureId}/questions")
    public ResponseEntity<ChatResponseDTO> sendQuestion(
            @RequestAttribute("email") String email,
            @PathVariable Long lectureId,
            @Valid @RequestBody ChatRequestDTO request) {

        logger.debug("질문 전송 요청: email={}, lectureId={}, question={}",
                email, lectureId, request.getQuestion());

        ChatResponseDTO response = chatService.sendQuestion(email, lectureId, request);

        logger.debug("질문 응답 완료: questionId={}, answerId={}",
                response.getQuestionId(), response.getAnswerId());

        return ResponseEntity.ok(response);
    }

    /**
     * 강의별 채팅 기록을 조회합니다.
     */
    @GetMapping("/lectures/{lectureId}/history")
    public ResponseEntity<ChatHistoryDTO> getChatHistory(
            @RequestAttribute("email") String email,
            @PathVariable Long lectureId) {

        logger.debug("채팅 기록 조회 요청: email={}, lectureId={}", email, lectureId);

        ChatHistoryDTO history = chatService.getChatHistory(email, lectureId);

        logger.debug("채팅 기록 조회 완료: lectureId={}, messageCount={}",
                history.getLectureId(), history.getMessages().size());

        return ResponseEntity.ok(history);
    }

    /**
     * 오류 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        logger.error("API 오류 발생: {}", e.getMessage(), e);

        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
} 