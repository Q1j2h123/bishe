package com.oj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {
    /**
     * 是否执行成功
     */
    private Boolean success;
    
    /**
     * 执行输出
     */
    private String output;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 执行时间(ms)
     */
    private Long executionTime;
    
    /**
     * 内存占用(KB)
     */
    private Long memoryUsage;
    
    /**
     * 执行状态
     */
    private String status;
} 