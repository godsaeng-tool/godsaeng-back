package com.example.godsaengbackend.service;

import com.example.godsaengbackend.dto.StudyPlanDto;
import com.example.godsaengbackend.dto.StudyRecordDto;
import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.entity.StudyPlan;
import com.example.godsaengbackend.entity.StudyRecord;
import com.example.godsaengbackend.entity.User;
import com.example.godsaengbackend.repository.LectureRepository;
import com.example.godsaengbackend.repository.StudyPlanRepository;
import com.example.godsaengbackend.repository.StudyRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyService {

    private final StudyPlanRepository studyPlanRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final LectureRepository lectureRepository;
    private final UserService userService;
    private final AIService aiService;

    public StudyService(StudyPlanRepository studyPlanRepository, 
                        StudyRecordRepository studyRecordRepository,
                        LectureRepository lectureRepository,
                        UserService userService,
                        AIService aiService) {
        this.studyPlanRepository = studyPlanRepository;
        this.studyRecordRepository = studyRecordRepository;
        this.lectureRepository = lectureRepository;
        this.userService = userService;
        this.aiService = aiService;
    }

    // 학습 계획 관련 메서드
    @Transactional
    public StudyPlanDto.Response createStudyPlan(String email, StudyPlanDto.CreateRequest request) {
        User user = userService.findByEmail(email);
        Lecture lecture = lectureRepository.findById(request.getLectureId())
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        
        // 사용자가 해당 강의의 소유자인지 확인
        if (!lecture.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 강의에 대한 권한이 없습니다.");
        }
        
        StudyPlan studyPlan = StudyPlan.builder()
                .user(user)
                .lecture(lecture)
                .planDetails(request.getPlanDetails())
                .status(request.getStatus() != null ? request.getStatus() : StudyPlan.Status.PENDING)
                .build();
        
        StudyPlan savedStudyPlan = studyPlanRepository.save(studyPlan);
        return StudyPlanDto.Response.fromEntity(savedStudyPlan);
    }

    @Transactional(readOnly = true)
    public Page<StudyPlanDto.Response> getStudyPlans(String email, Pageable pageable) {
        User user = userService.findByEmail(email);
        Page<StudyPlan> studyPlans = studyPlanRepository.findByUser(user, pageable);
        return studyPlans.map(StudyPlanDto.Response::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<StudyPlanDto.Response> getStudyPlansByLecture(String email, Long lectureId, Pageable pageable) {
        User user = userService.findByEmail(email);
        Page<StudyPlan> studyPlans = studyPlanRepository.findByUserAndLectureId(user, lectureId, pageable);
        return studyPlans.map(StudyPlanDto.Response::fromEntity);
    }

    @Transactional(readOnly = true)
    public StudyPlanDto.Response getStudyPlan(String email, Long planId) {
        User user = userService.findByEmail(email);
        StudyPlan studyPlan = studyPlanRepository.findByIdAndUser(planId, user)
                .orElseThrow(() -> new RuntimeException("학습 계획을 찾을 수 없습니다."));
        return StudyPlanDto.Response.fromEntity(studyPlan);
    }

    @Transactional
    public StudyPlanDto.Response updateStudyPlan(String email, Long planId, StudyPlanDto.UpdateRequest request) {
        User user = userService.findByEmail(email);
        StudyPlan studyPlan = studyPlanRepository.findByIdAndUser(planId, user)
                .orElseThrow(() -> new RuntimeException("학습 계획을 찾을 수 없습니다."));
        
        if (request.getPlanDetails() != null) {
            studyPlan.setPlanDetails(request.getPlanDetails());
        }
        if (request.getStatus() != null) {
            studyPlan.setStatus(request.getStatus());
        }
        
        StudyPlan updatedStudyPlan = studyPlanRepository.save(studyPlan);
        return StudyPlanDto.Response.fromEntity(updatedStudyPlan);
    }

    @Transactional
    public void deleteStudyPlan(String email, Long planId) {
        User user = userService.findByEmail(email);
        StudyPlan studyPlan = studyPlanRepository.findByIdAndUser(planId, user)
                .orElseThrow(() -> new RuntimeException("학습 계획을 찾을 수 없습니다."));
        
        studyPlanRepository.delete(studyPlan);
    }

    // 학습 기록 관련 메서드
    @Transactional
    public StudyRecordDto.Response createStudyRecord(String email, StudyRecordDto.CreateRequest request) {
        User user = userService.findByEmail(email);
        Lecture lecture = lectureRepository.findById(request.getLectureId())
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        
        // 사용자가 해당 강의의 소유자인지 확인
        if (!lecture.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 강의에 대한 권한이 없습니다.");
        }
        
        StudyRecord studyRecord = StudyRecord.builder()
                .user(user)
                .lecture(lecture)
                .studyDate(request.getStudyDate())
                .studyDuration(request.getStudyDuration())
                .notes(request.getNotes())
                .build();
        
        StudyRecord savedStudyRecord = studyRecordRepository.save(studyRecord);
        return StudyRecordDto.Response.fromEntity(savedStudyRecord);
    }

    @Transactional(readOnly = true)
    public Page<StudyRecordDto.Response> getStudyRecords(String email, Pageable pageable) {
        User user = userService.findByEmail(email);
        Page<StudyRecord> studyRecords = studyRecordRepository.findByUser(user, pageable);
        return studyRecords.map(StudyRecordDto.Response::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<StudyRecordDto.Response> getStudyRecordsByLecture(String email, Long lectureId, Pageable pageable) {
        User user = userService.findByEmail(email);
        Page<StudyRecord> studyRecords = studyRecordRepository.findByUserAndLectureId(user, lectureId, pageable);
        return studyRecords.map(StudyRecordDto.Response::fromEntity);
    }

    @Transactional(readOnly = true)
    public StudyRecordDto.Response getStudyRecord(String email, Long recordId) {
        User user = userService.findByEmail(email);
        StudyRecord studyRecord = studyRecordRepository.findByIdAndUser(recordId, user)
                .orElseThrow(() -> new RuntimeException("학습 기록을 찾을 수 없습니다."));
        return StudyRecordDto.Response.fromEntity(studyRecord);
    }

    @Transactional
    public StudyRecordDto.Response updateStudyRecord(String email, Long recordId, StudyRecordDto.UpdateRequest request) {
        User user = userService.findByEmail(email);
        StudyRecord studyRecord = studyRecordRepository.findByIdAndUser(recordId, user)
                .orElseThrow(() -> new RuntimeException("학습 기록을 찾을 수 없습니다."));
        
        if (request.getStudyDate() != null) {
            studyRecord.setStudyDate(request.getStudyDate());
        }
        if (request.getStudyDuration() != null) {
            studyRecord.setStudyDuration(request.getStudyDuration());
        }
        if (request.getNotes() != null) {
            studyRecord.setNotes(request.getNotes());
        }
        
        StudyRecord updatedStudyRecord = studyRecordRepository.save(studyRecord);
        return StudyRecordDto.Response.fromEntity(updatedStudyRecord);
    }

    @Transactional
    public void deleteStudyRecord(String email, Long recordId) {
        User user = userService.findByEmail(email);
        StudyRecord studyRecord = studyRecordRepository.findByIdAndUser(recordId, user)
                .orElseThrow(() -> new RuntimeException("학습 기록을 찾을 수 없습니다."));
        
        studyRecordRepository.delete(studyRecord);
    }
    
    // AI를 활용한 학습 계획 추천
    public String getStudyRecommendation(String email, Long lectureId) {
        // AI 서비스를 통해 학습 계획 추천 받기
        return aiService.getStudyRecommendation(email, lectureId);
    }
}
