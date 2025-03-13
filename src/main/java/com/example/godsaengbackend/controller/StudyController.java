package com.example.godsaengbackend.controller;

import com.example.godsaengbackend.dto.StudyPlanDto;
import com.example.godsaengbackend.dto.StudyRecordDto;
import com.example.godsaengbackend.service.StudyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    // 학습 계획 관련 API
    @PostMapping("/plans")
    public ResponseEntity<StudyPlanDto.Response> createStudyPlan(
            @RequestAttribute("email") String email,
            @Valid @RequestBody StudyPlanDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyService.createStudyPlan(email, request));
    }

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

    @PutMapping("/plans/{planId}")
    public ResponseEntity<StudyPlanDto.Response> updateStudyPlan(
            @RequestAttribute("email") String email,
            @PathVariable Long planId,
            @Valid @RequestBody StudyPlanDto.UpdateRequest request) {
        return ResponseEntity.ok(studyService.updateStudyPlan(email, planId, request));
    }

    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<Void> deleteStudyPlan(
            @RequestAttribute("email") String email,
            @PathVariable Long planId) {
        studyService.deleteStudyPlan(email, planId);
        return ResponseEntity.noContent().build();
    }

    // 학습 기록 관련 API
    @PostMapping("/records")
    public ResponseEntity<StudyRecordDto.Response> createStudyRecord(
            @RequestAttribute("email") String email,
            @Valid @RequestBody StudyRecordDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyService.createStudyRecord(email, request));
    }

    @GetMapping("/records")
    public ResponseEntity<Page<StudyRecordDto.Response>> getStudyRecords(
            @RequestAttribute("email") String email,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(studyService.getStudyRecords(email, pageable));
    }

    @GetMapping("/lectures/{lectureId}/records")
    public ResponseEntity<Page<StudyRecordDto.Response>> getStudyRecordsByLecture(
            @RequestAttribute("email") String email,
            @PathVariable Long lectureId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(studyService.getStudyRecordsByLecture(email, lectureId, pageable));
    }

    @GetMapping("/records/{recordId}")
    public ResponseEntity<StudyRecordDto.Response> getStudyRecord(
            @RequestAttribute("email") String email,
            @PathVariable Long recordId) {
        return ResponseEntity.ok(studyService.getStudyRecord(email, recordId));
    }

    @PutMapping("/records/{recordId}")
    public ResponseEntity<StudyRecordDto.Response> updateStudyRecord(
            @RequestAttribute("email") String email,
            @PathVariable Long recordId,
            @Valid @RequestBody StudyRecordDto.UpdateRequest request) {
        return ResponseEntity.ok(studyService.updateStudyRecord(email, recordId, request));
    }

    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<Void> deleteStudyRecord(
            @RequestAttribute("email") String email,
            @PathVariable Long recordId) {
        studyService.deleteStudyRecord(email, recordId);
        return ResponseEntity.noContent().build();
    }
    
    // AI 학습 추천 API
    @GetMapping("/recommendation/{lectureId}")
    public ResponseEntity<Map<String, String>> getStudyRecommendation(
            @RequestAttribute("email") String email,
            @PathVariable Long lectureId) {
        String recommendation = studyService.getStudyRecommendation(email, lectureId);
        
        Map<String, String> response = new HashMap<>();
        response.put("recommendation", recommendation);
        
        return ResponseEntity.ok(response);
    }
}
