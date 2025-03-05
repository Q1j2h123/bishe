package com.oj.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChoiceJudgeSubmission {
    private Long id;
    private Long userId;
    private Long problemId;
    private String answer;
    private Boolean isCorrect;
    private LocalDateTime submitTime;
} 