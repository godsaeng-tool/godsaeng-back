package com.example.godsaengbackend.service;

import com.example.godsaengbackend.dto.AICallbackDTO;
import com.example.godsaengbackend.dto.LectureDto;
import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.entity.User;
import com.example.godsaengbackend.repository.LectureRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class LectureService {

    private static final Logger logger = LoggerFactory.getLogger(LectureService.class);

    private final LectureRepository lectureRepository;
    private final UserService userService;
    private final AIService aiService;
    private final RestTemplate restTemplate;
    private final ChatService chatService;

    public LectureService(LectureRepository lectureRepository, UserService userService, AIService aiService, RestTemplate restTemplate, ChatService chatService) {
        this.lectureRepository = lectureRepository;
        this.userService = userService;
        this.aiService = aiService;
        this.restTemplate = restTemplate;
        this.chatService = chatService;
    }

    @Transactional
    public LectureDto.Response createLecture(String email, LectureDto.CreateRequest request) {
        User user = userService.findByEmail(email);

        Lecture lecture = Lecture.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .sourceType(request.getSourceType())
                .videoUrl(request.getVideoUrl())
                .status(Lecture.LectureStatus.PROCESSING)
                .embeddingSynced(false)
                .build();

        Lecture savedLecture = lectureRepository.save(lecture);

        // 비동기로 AI 처리 시작
        aiService.processLecture(savedLecture.getId(), savedLecture.getSourceType(), savedLecture.getVideoUrl());

        return LectureDto.Response.fromEntity(savedLecture);
    }

    @Transactional(readOnly = true)
    public Page<LectureDto.Response> getLectures(String email, Pageable pageable) {
        User user = userService.findByEmail(email);
        Page<Lecture> lectures = lectureRepository.findByUser(user, pageable);
        return lectures.map(LectureDto.Response::fromEntity);
    }

    @Transactional(readOnly = true)
    public LectureDto.DetailResponse getLecture(String email, Long lectureId) {
        User user = userService.findByEmail(email);
        Lecture lecture = lectureRepository.findByIdAndUser(lectureId, user)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        return LectureDto.DetailResponse.fromEntity(lecture);
    }

    @Transactional
    public void updateLectureStatus(Long lectureId, Lecture.LectureStatus status) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));

        lecture.setStatus(status);
        lectureRepository.save(lecture);
    }

    @Transactional
    public void updateLectureResult(Long lectureId, String transcript, String summary, String expectedQuestions) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));

        lecture.setTranscript(transcript);
        lecture.setSummary(summary);
        lecture.setExpectedQuestions(expectedQuestions);
        lecture.setStatus(Lecture.LectureStatus.COMPLETED);
        lectureRepository.save(lecture);
    }

    @Transactional
    public void updateEmbeddingStatus(Long lectureId, String vectorDbId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));

        lecture.setEmbeddingSynced(true);
        lecture.setVectorDbId(vectorDbId);
        lectureRepository.save(lecture);
    }

    @Transactional
    public void updateLectureWithAIResult(AICallbackDTO callbackData) {
        logger.debug("AI 콜백 데이터 수신: lecture_id={}, task_id={}", 
                    callbackData.getLecture_id(), callbackData.getTask_id());
        
        Lecture lecture = lectureRepository.findById(Long.parseLong(callbackData.getLecture_id()))
                .orElseThrow(() -> new EntityNotFoundException("강의를 찾을 수 없습니다."));

        // AI 처리 결과로 강의 업데이트
        lecture.setStatus(Lecture.LectureStatus.COMPLETED);
        lecture.setTranscript(callbackData.getTranscribed_text());
        lecture.setSummary(callbackData.getSummary_text());
        lecture.setExpectedQuestions(callbackData.getQuiz_text());
        lecture.setStudyPlan(callbackData.getStudy_plan());
        lecture.setTaskId(callbackData.getTask_id()); // 채팅을 위한 task_id 저장
        
        logger.debug("강의 업데이트: id={}, task_id={}", lecture.getId(), lecture.getTaskId());
        
        lectureRepository.save(lecture);
        
        // 강의 처리 완료 시 환영 메시지 자동 생성
        try {
            chatService.createWelcomeMessage(lecture.getId());
            logger.debug("환영 메시지 생성 완료: lectureId={}", lecture.getId());
        } catch (Exception e) {
            // 메시지 생성 실패해도 강의 처리는 완료된 것으로 간주
            logger.error("환영 메시지 생성 실패: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void updateLectureTaskId(Long lectureId, String taskId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        
        lecture.setTaskId(taskId);
        lecture.setVideoUrl(taskId); // videoUrl에도 taskId 저장
        lectureRepository.save(lecture);
    }

}
