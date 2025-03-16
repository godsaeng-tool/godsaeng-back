package com.example.godsaengbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AICallbackDTO {
    private String task_id;
    private String lecture_id;
    private String status;
    private String message;
    private String transcribed_text;
    private String summary_text;
    private String quiz_text;
    private String study_plan;
} 