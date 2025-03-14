package com.example.godsaengbackend.service;

import com.example.godsaengbackend.entity.ChatMessage;
import com.example.godsaengbackend.entity.ChatSession;
import com.example.godsaengbackend.entity.Lecture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void processLecture(Long lectureId, Lecture.SourceType sourceType, String videoUrl) {
        try {
            // AI 서비스에 요청 보내기
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("lecture_id", lectureId);
            requestBody.put("source_type", sourceType.toString());
            
            if (sourceType == Lecture.SourceType.YOUTUBE) {
                requestBody.put("youtube_url", videoUrl);
            } else {
                // 파일 업로드의 경우 파일 경로 전송
                requestBody.put("file_url", videoUrl);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // AI 서비스에 비동기 처리 요청
            restTemplate.postForObject(aiServiceUrl + "/process", request, Void.class);
            
            logger.info("강의 ID {}에 대한 AI 처리 요청을 전송했습니다.", lectureId);
            
        } catch (Exception e) {
            logger.error("AI 처리 요청 중 오류 발생: {}", e.getMessage());
        }
    }
    
    // 파일 업로드를 위한 메서드 - 파일을 직접 Flask 서버로 전송
    public String uploadFileToAIService(MultipartFile file) {
        try {
            // 파일 데이터를 MultipartFile에서 추출
            String filename = file.getOriginalFilename();
            byte[] fileBytes = file.getBytes();
            
            // MultiValueMap 생성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // 파일 데이터 추가
            ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
            body.add("file", resource);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // HTTP 요청 생성
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Flask 서버로 요청 전송
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/upload", requestEntity, Map.class);
            
            // 응답에서 파일 URL 또는 식별자 추출
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("file_url");
            } else {
                throw new RuntimeException("파일 업로드 실패: 서버 응답이 유효하지 않습니다.");
            }
        } catch (Exception e) {
            logger.error("파일 업로드 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    // 학습 계획 추천 요청 - 비동기로 처리하고 콜백으로 결과 받기
    @Async
    public void requestStudyPlanRecommendation(String email, Long lectureId) {
        try {
            // AI 서비스에 요청 보내기
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("lecture_id", lectureId);
            requestBody.put("callback_url", aiServiceUrl + "/api/ai/callback/study-plan");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // AI 서비스에 비동기 처리 요청
            restTemplate.postForObject(aiServiceUrl + "/recommendation-async", request, Void.class);
            
            logger.info("강의 ID {}에 대한 학습 계획 추천 요청을 전송했습니다.", lectureId);
            
        } catch (Exception e) {
            logger.error("학습 계획 추천 요청 중 오류 발생: {}", e.getMessage());
        }
    }

    // 동기 방식의 학습 계획 추천 요청 (기존 메서드 유지)
    public String getStudyRecommendation(String email, Long lectureId) {
        try {
            // AI 서비스에 요청 보내기
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("lecture_id", lectureId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // AI 서비스에 요청 전송
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/recommendation", request, Map.class);
            
            // 응답에서 추천 내용 추출
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("recommendation");
            } else {
                throw new RuntimeException("학습 추천 실패: 서버 응답이 유효하지 않습니다.");
            }
        } catch (Exception e) {
            logger.error("학습 추천 요청 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("학습 추천 실패", e);
        }
    }

    /**
     * AI 서버에 채팅 응답을 요청합니다. (말투 정보 포함)
     */
    public String requestChatResponse(Long lectureId, Long sessionId, String userMessage,
                                      List<ChatMessage> previousMessages, ChatSession.ToneType tone) {
        try {
            // 이전 메시지 히스토리 구성
            List<Map<String, String>> messageHistory = previousMessages.stream()
                    .map(msg -> Map.of(
                            "role", msg.getRole().toString().toLowerCase(),
                            "content", msg.getContent()
                    ))
                    .collect(Collectors.toList());
            
            // AI 서비스에 요청 보내기
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("lecture_id", lectureId);
            requestBody.put("session_id", sessionId);
            requestBody.put("message", userMessage);
            requestBody.put("history", messageHistory);
            requestBody.put("tone", tone.name());  // 말투 정보 추가
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // AI 서비스에 요청 전송
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/chat", request, Map.class);
            
            // 응답에서 AI 메시지 추출
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("response");
            } else {
                logger.error("AI 응답 실패: 서버 응답이 유효하지 않습니다.");
                return "죄송합니다. 현재 응답을 생성할 수 없습니다. 나중에 다시 시도해주세요.";
            }
        } catch (Exception e) {
            logger.error("AI 채팅 요청 중 오류 발생: {}", e.getMessage());
            return "죄송합니다. 서버 오류가 발생했습니다. 나중에 다시 시도해주세요.";
        }
    }
}
