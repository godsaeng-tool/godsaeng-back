package com.example.godsaengbackend.controller;

import com.example.godsaengbackend.dto.LectureDto;
import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.service.AIService;
import com.example.godsaengbackend.service.LectureService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/lectures")
public class LectureController {

    private final LectureService lectureService;
    private final AIService aiService;

    public LectureController(LectureService lectureService, AIService aiService) {
        this.lectureService = lectureService;
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<LectureDto.Response> createLecture(
            @RequestAttribute("email") String email,
            @Valid @RequestBody LectureDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lectureService.createLecture(email, request));
    }

    @PostMapping("/youtube")
    public ResponseEntity<LectureDto.Response> createYoutubeLecture(
            @RequestAttribute("email") String email,
            @Valid @RequestBody LectureDto.CreateRequest request) {
        // YouTube URL 유효성 검사
        if (request.getSourceType() != Lecture.SourceType.YOUTUBE ||
                !request.getVideoUrl().contains("youtube.com") && !request.getVideoUrl().contains("youtu.be")) {
            throw new IllegalArgumentException("유효한 YouTube URL이 아닙니다.");
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lectureService.createLecture(email, request));
    }

    @PostMapping("/upload")
    public ResponseEntity<LectureDto.Response> uploadLecture(
            @RequestAttribute("email") String email,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description) {
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        
        // 파일을 AI 서비스로 직접 업로드하고 URL 또는 식별자 받기
        String fileUrl = aiService.uploadFileToAIService(file);
        
        // 강의 생성 요청 객체 생성
        LectureDto.CreateRequest request = LectureDto.CreateRequest.builder()
                .title(title)
                .description(description)
                .sourceType(Lecture.SourceType.UPLOAD)
                .videoUrl(fileUrl)  // AI 서비스에서 반환한 URL 또는 식별자
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lectureService.createLecture(email, request));
    }

    @GetMapping
    public ResponseEntity<Page<LectureDto.Response>> getLectures(
            @RequestAttribute("email") String email,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(lectureService.getLectures(email, pageable));
    }

    @GetMapping("/{lectureId}")
    public ResponseEntity<LectureDto.DetailResponse> getLecture(
            @RequestAttribute("email") String email,
            @PathVariable Long lectureId) {
        return ResponseEntity.ok(lectureService.getLecture(email, lectureId));
    }
}
