package com.oj.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 测试用例执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestcaseResult implements Serializable {
    /**
     * 测试用例ID
     *
     */
    @JsonIgnore
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

    /**
     * 测试用例状态
     */
    private String status;

    private static final long serialVersionUID = 1L;
} 