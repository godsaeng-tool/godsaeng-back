package com.example.godsaengbackend.dto;

import com.example.godsaengbackend.entity.StudyRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class StudyRecordDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "강의 ID는 필수 입력값입니다.")
        private Long lectureId;
        
        @NotNull(message = "학습 날짜는 필수 입력값입니다.")
        private LocalDateTime studyDate;
        
        private Integer studyDuration;
        
        private String notes;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long userId;
        private Long lectureId;
        private String lectureTitle;
        private LocalDateTime studyDate;
        private Integer studyDuration;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public static Response fromEntity(StudyRecord studyRecord) {
            return Response.builder()
                    .id(studyRecord.getId())
                    .userId(studyRecord.getUser().getId())
                    .lectureId(studyRecord.getLecture().getId())
                    .lectureTitle(studyRecord.getLecture().getTitle())
                    .studyDate(studyRecord.getStudyDate())
                    .studyDuration(studyRecord.getStudyDuration())
                    .notes(studyRecord.getNotes())
                    .createdAt(studyRecord.getCreatedAt())
                    .updatedAt(studyRecord.getUpdatedAt())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private LocalDateTime studyDate;
        private Integer studyDuration;
        private String notes;
    }
}
