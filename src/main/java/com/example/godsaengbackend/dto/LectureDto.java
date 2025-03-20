package com.example.godsaengbackend.dto;

import com.example.godsaengbackend.entity.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class LectureDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수 입력값입니다.")
        private String title;
        
        private String description;
        
        @NotNull(message = "소스 타입은 필수 입력값입니다.")
        private Lecture.SourceType sourceType;
        
        private String videoUrl;
        
        private String studyPlan;
        
        private Integer remainingDays;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private Lecture.SourceType sourceType;
        private String videoUrl;
        private Lecture.LectureStatus status;
        private Boolean embeddingSynced;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Integer remainingDays;
        
        public static Response fromEntity(Lecture lecture) {
            return Response.builder()
                    .id(lecture.getId())
                    .title(lecture.getTitle())
                    .description(lecture.getDescription())
                    .sourceType(lecture.getSourceType())
                    .videoUrl(lecture.getVideoUrl())
                    .status(lecture.getStatus())
                    .embeddingSynced(lecture.getEmbeddingSynced())
                    .createdAt(lecture.getCreatedAt())
                    .updatedAt(lecture.getUpdatedAt())
                    .remainingDays(lecture.getRemainingDays())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse {
        private Long id;
        private String title;
        private String description;
        private Lecture.SourceType sourceType;
        private String videoUrl;
        private String transcript;
        private String summary;
        private String expectedQuestions;
        private Lecture.LectureStatus status;
        private Boolean embeddingSynced;
        private LocalDateTime createdAt;
        private String studyPlan;
        private String taskId;
        private Integer remainingDays;
        
        public static DetailResponse fromEntity(Lecture lecture) {
            return DetailResponse.builder()
                    .id(lecture.getId())
                    .title(lecture.getTitle())
                    .description(lecture.getDescription())
                    .sourceType(lecture.getSourceType())
                    .videoUrl(lecture.getVideoUrl())
                    .transcript(lecture.getTranscript())
                    .summary(lecture.getSummary())
                    .expectedQuestions(lecture.getExpectedQuestions())
                    .status(lecture.getStatus())
                    .embeddingSynced(lecture.getEmbeddingSynced())
                    .createdAt(lecture.getCreatedAt())
                    .studyPlan(lecture.getStudyPlan())
                    .taskId(lecture.getTaskId())
                    .remainingDays(lecture.getRemainingDays())
                    .build();
        }
    }
}
