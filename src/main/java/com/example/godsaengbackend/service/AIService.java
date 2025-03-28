package com.example.godsaengbackend.service;

import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.repository.LectureRepository;
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
    private final LectureRepository lectureRepository;
    
    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public AIService(RestTemplate restTemplate, LectureRepository lectureRepository) {
        this.restTemplate = restTemplate;
        this.lectureRepository = lectureRepository;
    }

    @Async
    public void processLecture(Long lectureId, Lecture.SourceType sourceType, String videoUrl, Integer remainingDays) {
        try {
            // 강의 정보 조회
           
            // AI 서비스에 요청 보내기
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("lecture_id", lectureId);
            requestBody.put("source_type", sourceType.toString());
            
            // 남은 일수 정보 추가
            if (remainingDays != null) {
                requestBody.put("remaining_days", remainingDays);
            }
            
            // 콜백 URL 추가 - AI 서버가 처리 완료 후 이 URL로 결과를 보냄
            String callbackUrl = "http://localhost:8080/api/ai/callback/complete";
            requestBody.put("callback_url", callbackUrl);
            
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
    public String uploadFileToAIService(MultipartFile file, Long lectureId) {
        try {
            // 강의 정보 조회
            Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다: " + lectureId));
            
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
            
            // lecture_id 추가 - 중요!
            body.add("lecture_id", lectureId.toString());
            
            // 남은 일수 정보 추가
            if (lecture.getRemainingDays() != null) {
                body.add("remaining_days", lecture.getRemainingDays().toString());
            }
            
            // 콜백 URL 추가
            String callbackUrl = "http://localhost:8080/api/ai/callback/complete";
            body.add("callback_url", callbackUrl);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // HTTP 요청 생성
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Flask 서버로 요청 전송
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/process", requestEntity, Map.class);
            
            // 응답에서 파일 URL 또는 식별자 추출
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                if (response.getBody().containsKey("task_id")) {
                    return (String) response.getBody().get("task_id");
                } else if (response.getBody().containsKey("file_url")) {
                    return (String) response.getBody().get("file_url");
                } else {
                    throw new RuntimeException("파일 업로드 실패: 응답에서 task_id 또는 file_url을 찾을 수 없습니다.");
                }
            } else {
                throw new RuntimeException("파일 업로드 실패: 서버 응답이 유효하지 않습니다.");
            }
        } catch (Exception e) {
            logger.error("파일 업로드 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }




}
