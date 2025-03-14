package com.example.godsaengbackend.controller;

import com.example.godsaengbackend.dto.ChatDto;
import com.example.godsaengbackend.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<ChatDto.SessionResponse> createChatSession(
            @RequestAttribute("email") String email,
            @Valid @RequestBody ChatDto.CreateSessionRequest request) {
        return ResponseEntity.ok(chatService.createChatSession(email, request));
    }

    @GetMapping("/sessions")
    public ResponseEntity<Page<ChatDto.SessionResponse>> getChatSessions(
            @RequestAttribute("email") String email,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(chatService.getChatSessions(email, pageable));
    }

    @GetMapping("/lectures/{lectureId}/sessions")
    public ResponseEntity<Page<ChatDto.SessionResponse>> getChatSessionsByLecture(
            @RequestAttribute("email") String email,
            @PathVariable Long lectureId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(chatService.getChatSessionsByLecture(email, lectureId, pageable));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatDto.SessionDetailResponse> getChatSession(
            @RequestAttribute("email") String email,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getChatSession(email, sessionId));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatDto.MessageResponse> sendMessage(
            @RequestAttribute("email") String email,
            @PathVariable Long sessionId,
            @Valid @RequestBody ChatDto.MessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(email, sessionId, request));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteChatSession(
            @RequestAttribute("email") String email,
            @PathVariable Long sessionId) {
        chatService.deleteChatSession(email, sessionId);
        return ResponseEntity.noContent().build();
    }

    // 모든 말투 옵션 조회
    @GetMapping("/tones")
    public ResponseEntity<List<ChatDto.ToneOption>> getToneOptions() {
        return ResponseEntity.ok(chatService.getToneOptions());
    }
    
    // 사용자가 사용 가능한 말투 옵션 조회
    @GetMapping("/available-tones")
    public ResponseEntity<List<ChatDto.ToneOption>> getAvailableToneOptions(
            @RequestAttribute("email") String email) {
        return ResponseEntity.ok(chatService.getAvailableToneOptions(email));
    }
} 