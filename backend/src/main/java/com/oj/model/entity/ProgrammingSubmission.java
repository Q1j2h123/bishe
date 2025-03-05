package com.oj.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProgrammingSubmission {
    private Long id;
    private Long userId;
    private Long problemId;
    private String language;
    private String code;
    private String status;  // ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED等
    private Integer executeTime;  // 执行时间(ms)
    private Integer memoryUsage;  // 内存使用(MB)
    private LocalDateTime submitTime;
} 