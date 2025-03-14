package com.example.godsaengbackend.controller;

import com.example.godsaengbackend.dto.StudyPlanDto;
import com.example.godsaengbackend.entity.StudyPlan;
import com.example.godsaengbackend.service.StudyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    // AI 학습 추천 API - 추천 받고 바로 저장하는 방식으로 변경
    @GetMapping("/recommendation/{lectureId}")
    public ResponseEntity<StudyPlanDto.Response> getStudyRecommendation(
            @RequestAttribute("email") String email,
            @PathVariable Long lectureId) {
        // AI 추천을 받아서 바로 StudyPlan으로 저장
        StudyPlanDto.Response savedPlan = studyService.createStudyPlanFromAI(email, lectureId);
        return ResponseEntity.ok(savedPlan);
    }

    // 학습 계획 조회 API - 조회만 가능
    @GetMapping("/plans")
    public ResponseEntity<Page<StudyPlanDto.Response>> getStudyPlans(
            @RequestAttribute("email") String email,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(studyService.getStudyPlans(email, pageable));
    }

    @GetMapping("/lectures/{lectureId}/plans")
    public ResponseEntity<Page<StudyPlanDto.Response>> getStudyPlansByLecture(
            @RequestAttribute("email") String email,
            @PathVariable Long lectureId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(studyService.getStudyPlansByLecture(email, lectureId, pageable));
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<StudyPlanDto.Response> getStudyPlan(
            @RequestAttribute("email") String email,
            @PathVariable Long planId) {
        return ResponseEntity.ok(studyService.getStudyPlan(email, planId));
    }
    
    // 학습 계획 상태만 업데이트하는 API
    @PatchMapping("/plans/{planId}/status")
    public ResponseEntity<StudyPlanDto.Response> updateStudyPlanStatus(
            @RequestAttribute("email") String email,
            @PathVariable Long planId,
            @RequestBody Map<String, String> statusRequest) {
        StudyPlan.Status status = StudyPlan.Status.valueOf(statusRequest.get("status"));
        return ResponseEntity.ok(studyService.updateStudyPlanStatus(email, planId, status));
    }

    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<Void> deleteStudyPlan(
            @RequestAttribute("email") String email,
            @PathVariable Long planId) {
        studyService.deleteStudyPlan(email, planId);
        return ResponseEntity.noContent().build();
    }

    // 특정 강의의 학습 계획 조회 (목록이 아닌 단일 항목)
    @GetMapping("/lectures/{lectureId}/plan")
    public ResponseEntity<StudyPlanDto.Response> getStudyPlanByLecture(
            @RequestAttribute("email") String email,
            @PathVariable Long lectureId) {
        return ResponseEntity.ok(studyService.getStudyPlanByLecture(email, lectureId));
    }
}
