package com.example.godsaengbackend.service;

import com.example.godsaengbackend.dto.ChatHistoryDTO;
import com.example.godsaengbackend.dto.ChatRequestDTO;
import com.example.godsaengbackend.dto.ChatResponseDTO;
import com.example.godsaengbackend.entity.ChatMessage;
import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.entity.User;
import com.example.godsaengbackend.repository.ChatMessageRepository;
import com.example.godsaengbackend.repository.LectureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final LectureRepository lectureRepository;
    private final UserService userService;
    private final RestTemplate restTemplate;
    
    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public ChatService(
            ChatMessageRepository chatMessageRepository,
            LectureRepository lectureRepository,
            UserService userService,
            RestTemplate restTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.lectureRepository = lectureRepository;
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    /**
     * 강의에 대한 질문을 전송하고 AI 응답을 받아 저장합니다.
     */
    @Transactional
    public ChatResponseDTO sendQuestion(String email, Long lectureId, ChatRequestDTO request) {
        User user = userService.findByEmail(email);
        
        // 강의 정보 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        
        // 사용자가 해당 강의의 소유자인지 확인
        if (!lecture.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 강의에 대한 권한이 없습니다.");
        }
        
        // 강의의 task_id 확인
        String taskId = lecture.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            throw new RuntimeException("AI 처리가 완료되지 않은 강의입니다.");
        }
        
        // 사용자 질문 저장
        ChatMessage questionMessage = ChatMessage.builder()
                .lecture(lecture)
                .user(user)
                .role(ChatMessage.MessageRole.USER)
                .content(request.getQuestion())
                .build();
        
        ChatMessage savedQuestion = chatMessageRepository.save(questionMessage);
        logger.debug("사용자 질문 저장 완료: id={}, content={}", savedQuestion.getId(), savedQuestion.getContent());
        
        // AI 서버에 질문 전송
        String aiResponse = requestAIResponse(taskId, request.getQuestion(), request.getTone());
        
        // AI 응답 저장
        ChatMessage answerMessage = ChatMessage.builder()
                .lecture(lecture)
                .role(ChatMessage.MessageRole.ASSISTANT)
                .content(aiResponse)
                .parentId(savedQuestion.getId())  // 질문-답변 연결
                .build();
        
        ChatMessage savedAnswer = chatMessageRepository.save(answerMessage);
        logger.debug("AI 응답 저장 완료: id={}, parentId={}", savedAnswer.getId(), savedAnswer.getParentId());
        
        // 응답 DTO 생성
        return ChatResponseDTO.builder()
                .questionId(savedQuestion.getId())
                .answerId(savedAnswer.getId())
                .question(savedQuestion.getContent())
                .answer(savedAnswer.getContent())
                .timestamp(savedAnswer.getCreatedAt())
                .build();
    }
    
    /**
     * 강의별 채팅 기록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public ChatHistoryDTO getChatHistory(String email, Long lectureId) {
        User user = userService.findByEmail(email);
        
        // 강의 정보 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        
        // 사용자가 해당 강의의 소유자인지 확인
        if (!lecture.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 강의에 대한 권한이 없습니다.");
        }
        
        // 채팅 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findByLectureIdOrderByIdAsc(lectureId);
        
        // 질문-답변 쌍으로 변환
        List<ChatResponseDTO> chatResponses = new ArrayList<>();
        
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            
            if (message.getRole() == ChatMessage.MessageRole.USER) {
                // 사용자 질문 메시지인 경우
                ChatMessage answerMessage = null;
                
                // 다음 메시지가 있고 해당 메시지가 이 질문에 대한 답변인지 확인
                if (i + 1 < messages.size() && 
                    messages.get(i + 1).getRole() == ChatMessage.MessageRole.ASSISTANT &&
                    messages.get(i + 1).getParentId() != null &&
                    messages.get(i + 1).getParentId().equals(message.getId())) {
                    
                    answerMessage = messages.get(i + 1);
                    i++; // 답변 메시지는 이미 처리했으므로 인덱스 증가
                }
                
                ChatResponseDTO responseDTO = ChatResponseDTO.builder()
                        .questionId(message.getId())
                        .answerId(answerMessage != null ? answerMessage.getId() : null)
                        .question(message.getContent())
                        .answer(answerMessage != null ? answerMessage.getContent() : "응답을 받지 못했습니다.")
                        .timestamp(message.getCreatedAt())
                        .build();
                
                chatResponses.add(responseDTO);
            }
        }
        
        return ChatHistoryDTO.builder()
                .lectureId(lecture.getId())
                .lectureTitle(lecture.getTitle())
                .messages(chatResponses)
                .build();
    }
    
    /**
     * AI 서버에 질문을 전송하고 응답을 받습니다.
     */
    private String requestAIResponse(String taskId, String question, String tone) {
        try {
            // AI 서버 URL 설정
            String queryUrl = aiServiceUrl + "/query";
            
            // 요청 본문 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("task_id", taskId);
            requestBody.put("question", question);
            
            // 말투 설정 (선택적)
            if (tone != null && !tone.isEmpty() && !tone.equalsIgnoreCase("normal")) {
                requestBody.put("tone", tone.toLowerCase());
            }
            
            // 요청 로깅
            logger.debug("AI 서버 요청: URL={}, task_id={}, question={}", queryUrl, taskId, question);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // HTTP 요청 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // AI 서버에 요청 전송 - String으로 응답 받기
            ResponseEntity<String> response = restTemplate.postForEntity(queryUrl, requestEntity, String.class);
            
            // 응답 로깅
            logger.debug("AI 서버 응답: status={}", response.getStatusCode());
            
            // 응답이 JSON 형식인지 확인
            if (response.getBody() != null) {
                try {
                    // JSON 파싱 시도
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.getBody());
                    
                    // 응답이 배열 형태인지 확인
                    if (rootNode.isArray() && rootNode.size() > 0) {
                        // 배열의 첫 번째 요소 가져오기
                        JsonNode firstElement = rootNode.get(0);
                        
                        // data.answer 필드 추출 시도
                        if (firstElement.has("data") && firstElement.get("data").has("answer")) {
                            return firstElement.get("data").get("answer").asText();
                        } else if (firstElement.has("answer")) {
                            // 또는 직접 answer 필드가 있는 경우
                            return firstElement.get("answer").asText();
                        }
                    } else {
                        // 기존 로직 유지 (배열이 아닌 경우)
                        if (rootNode.has("data") && rootNode.get("data").has("answer")) {
                            return rootNode.get("data").get("answer").asText();
                        } else if (rootNode.has("answer")) {
                            return rootNode.get("answer").asText();
                        }
                    }
                } catch (Exception e) {
                    // JSON 파싱 실패 시 원본 응답 반환
                    logger.warn("JSON 파싱 실패, 원본 응답 반환: {}", e.getMessage());
                    return response.getBody();
                }
            }
            
            return "AI 서버에서 응답을 받았지만 형식이 올바르지 않습니다.";
        } catch (Exception e) {
            logger.error("AI 응답 요청 실패: {}", e.getMessage(), e);
            return "죄송합니다. AI 응답을 받아오는 데 실패했습니다: " + e.getMessage();
        }
    }
    
    /**
     * 강의 처리 완료 시 자동으로 환영 메시지를 생성합니다.
     */
    @Transactional
    public void createWelcomeMessage(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        
        // 이미 메시지가 있는지 확인
        List<ChatMessage> existingMessages = chatMessageRepository.findByLectureIdOrderByIdAsc(lectureId);
        
        if (existingMessages.isEmpty()) {
            // 환영 메시지 추가
            ChatMessage welcomeMessage = ChatMessage.builder()
                    .lecture(lecture)
                    .role(ChatMessage.MessageRole.ASSISTANT)
                    .content("안녕하세요! 이 강의에 대해 궁금한 점이 있으면 질문해주세요.")
                    .build();
            
            chatMessageRepository.save(welcomeMessage);
            logger.debug("강의 ID {}에 환영 메시지 생성 완료", lectureId);
        }
    }

    /**
     * 강의에 속한 모든 채팅 메시지를 삭제합니다.
     */
    @Transactional
    public void deleteAllChatsByLectureId(Long lectureId) {
        // void 메서드이므로 반환값을 받지 않음
        chatMessageRepository.deleteAllByLectureId(lectureId);
        logger.info("강의 ID {}에 속한 채팅 메시지 삭제 완료", lectureId);
    }
} 