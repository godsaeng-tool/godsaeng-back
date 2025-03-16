package com.example.godsaengbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private Long questionId;
    private Long answerId;
    private String question;
    private String answer;
    private LocalDateTime timestamp;
} 