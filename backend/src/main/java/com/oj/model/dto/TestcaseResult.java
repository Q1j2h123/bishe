package com.oj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试用例执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestcaseResult {
    /**
     * 测试用例ID
     */
    private Long testcaseId;
    
    /**
     * 输入数据
     */
    private String input;
    
    /**
     * 期望输出
     */
    private String expectedOutput;
    
    /**
     * 实际输出
     */
    private String actualOutput;
    
    /**
     * 是否通过
     */
    private Boolean passed;
    
    /**
     * 执行时间(ms)
     */
    private Long executionTime;
    
    /**
     * 内存占用(KB)
     */
    private Long memoryUsage;
    
    /**
     * 错误信息
     */
    private String errorMessage;
} 