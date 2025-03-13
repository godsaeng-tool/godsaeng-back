package com.example.godsaengbackend.dto;

import com.example.godsaengbackend.entity.StudyPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class StudyPlanDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "강의 ID는 필수 입력값입니다.")
        private Long lectureId;
        
        @NotBlank(message = "학습 계획 상세 내용은 필수 입력값입니다.")
        private String planDetails;
        
        private StudyPlan.Status status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long userId;
        private Long lectureId;
        private String lectureName;
        private String planDetails;
        private StudyPlan.Status status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public static Response fromEntity(StudyPlan studyPlan) {
            return Response.builder()
                    .id(studyPlan.getId())
                    .userId(studyPlan.getUser().getId())
                    .lectureId(studyPlan.getLecture().getId())
                    .lectureName(studyPlan.getLecture().getTitle())
                    .planDetails(studyPlan.getPlanDetails())
                    .status(studyPlan.getStatus())
                    .createdAt(studyPlan.getCreatedAt())
                    .updatedAt(studyPlan.getUpdatedAt())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String planDetails;
        private StudyPlan.Status status;
    }
}
