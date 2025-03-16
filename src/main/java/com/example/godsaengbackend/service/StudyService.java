package com.example.godsaengbackend.service;

import com.example.godsaengbackend.dto.StudyPlanDto;
import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.entity.StudyPlan;
import com.example.godsaengbackend.entity.User;
import com.example.godsaengbackend.repository.LectureRepository;
import com.example.godsaengbackend.repository.StudyPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyService {

    private static final Logger logger = LoggerFactory.getLogger(StudyService.class);

    private final StudyPlanRepository studyPlanRepository;
    private final LectureRepository lectureRepository;
    private final UserService userService;
    private final AIService aiService;

    public StudyService(StudyPlanRepository studyPlanRepository,
                        LectureRepository lectureRepository,
                        UserService userService,
                        AIService aiService) {
        this.studyPlanRepository = studyPlanRepository;
        this.lectureRepository = lectureRepository;
        this.userService = userService;
        this.aiService = aiService;
    }

    // 학습 계획 관련 메서드
    @Transactional
    public StudyPlanDto.Response createStudyPlan(String email, StudyPlanDto.CreateRequest request) {
        User user = userService.findByEmail(email);
        
        // God 모드 확인
        if (!user.getIsGodMode()) {
            throw new RuntimeException("구독 사용자만 학습 계획을 생성할 수 있습니다.");
        }
        
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

    // AI 추천을 받아서 바로 StudyPlan으로 저장하는 메서드
    @Transactional
    public StudyPlanDto.Response createStudyPlanFromAI(String email, Long lectureId) {
        User user = userService.findByEmail(email);
        
        // God 모드 확인
        if (!user.getIsGodMode()) {
            throw new RuntimeException("구독 사용자만 학습 계획 추천을 받을 수 있습니다.");
        }
        
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        
        // 사용자가 해당 강의의 소유자인지 확인
        if (!lecture.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 강의에 대한 권한이 없습니다.");
        }
        
        // AI 서비스에 비동기 요청 전송
        aiService.requestStudyPlanRecommendation(email, lectureId);
        
        // 임시 응답 반환 (실제 계획은 콜백으로 저장됨)
        return new StudyPlanDto.Response(
            null, 
            user.getId(), 
            lectureId, 
            lecture.getTitle(),
            "학습 계획을 생성 중입니다. 잠시 후 확인해주세요.",
            StudyPlan.Status.PENDING,
            null,
            null
        );
    }

    // 학습 계획 상태만 업데이트하는 메서드
    @Transactional
    public StudyPlanDto.Response updateStudyPlanStatus(String email, Long planId, StudyPlan.Status status) {
        User user = userService.findByEmail(email);
        StudyPlan studyPlan = studyPlanRepository.findByIdAndUser(planId, user)
                .orElseThrow(() -> new RuntimeException("학습 계획을 찾을 수 없습니다."));
        
        studyPlan.setStatus(status);
        StudyPlan updatedStudyPlan = studyPlanRepository.save(studyPlan);
        return StudyPlanDto.Response.fromEntity(updatedStudyPlan);
    }

    // AI 콜백으로 학습 계획 저장
    @Transactional
    public void saveAIGeneratedStudyPlan(String email, Long lectureId, String planDetails) {
        User user = userService.findByEmail(email);
        
        // God 모드 확인
        if (!user.getIsGodMode()) {
            logger.warn("구독 사용자가 아닌 사용자({})에 대한 학습 계획 생성 요청이 무시되었습니다.", email);
            return;
        }
        
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        
        // 사용자가 해당 강의의 소유자인지 확인
        if (!lecture.getUser().getId().equals(user.getId())) {
            logger.warn("사용자({})가 소유하지 않은 강의({})에 대한 학습 계획 생성 요청이 무시되었습니다.", email, lectureId);
            return;
        }
        
        // 기존 학습 계획이 있는지 확인
        StudyPlan existingPlan = studyPlanRepository.findByUserAndLecture(user, lecture).orElse(null);
        
        if (existingPlan != null) {
            // 기존 계획이 있으면 업데이트
            existingPlan.setPlanDetails(planDetails);
            existingPlan.setStatus(StudyPlan.Status.PENDING);
            studyPlanRepository.save(existingPlan);
            logger.info("강의 ID {}에 대한 기존 학습 계획이 업데이트되었습니다.", lectureId);
        } else {
            // 새 계획 생성
            StudyPlan studyPlan = StudyPlan.builder()
                    .user(user)
                    .lecture(lecture)
                    .planDetails(planDetails)
                    .status(StudyPlan.Status.PENDING)
                    .build();
            
            studyPlanRepository.save(studyPlan);
            logger.info("강의 ID {}에 대한 새 학습 계획이 저장되었습니다.", lectureId);
        }
    }

    @Transactional(readOnly = true)
    public StudyPlanDto.Response getStudyPlanByLecture(String email, Long lectureId) {
        User user = userService.findByEmail(email);
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        
        // 사용자가 해당 강의의 소유자인지 확인
        if (!lecture.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 강의에 대한 권한이 없습니다.");
        }
        
        StudyPlan studyPlan = studyPlanRepository.findByUserAndLecture(user, lecture)
                .orElseThrow(() -> new RuntimeException("해당 강의에 대한 학습 계획이 없습니다."));
        
        return StudyPlanDto.Response.fromEntity(studyPlan);
    }
}
