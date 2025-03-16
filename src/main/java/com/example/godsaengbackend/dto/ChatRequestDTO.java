package com.example.godsaengbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
    @NotBlank(message = "질문 내용은 필수입니다")
    private String question;
    
    private String tone;  // 선택적 말투 설정 (null이면 기본 말투)
} 