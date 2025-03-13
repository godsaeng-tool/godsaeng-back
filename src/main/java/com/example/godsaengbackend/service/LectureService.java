package com.example.godsaengbackend.service;

import com.example.godsaengbackend.dto.LectureDto;
import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.entity.User;
import com.example.godsaengbackend.repository.LectureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LectureService {

    private final LectureRepository lectureRepository;
    private final UserService userService;
    private final AIService aiService;

    public LectureService(LectureRepository lectureRepository, UserService userService, AIService aiService) {
        this.lectureRepository = lectureRepository;
        this.userService = userService;
        this.aiService = aiService;
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
}
