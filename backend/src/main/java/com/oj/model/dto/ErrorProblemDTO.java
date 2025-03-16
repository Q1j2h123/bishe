package com.oj.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 错题记录DTO
 */
@Data
public class ErrorProblemDTO {
    /**
     * 题目ID
     */
    private Long id;
    
    /**
     * 题目标题
     */
    private String title;
    
    /**
     * 题目类型
     */
    private String type;
    
    /**
     * 题目难度
     */
    private String difficulty;
    
    /**
     * 题目标签
     */
    private String tags;
    
    /**
     * 最近错误时间
     */
    private LocalDateTime lastErrorTime;
} 