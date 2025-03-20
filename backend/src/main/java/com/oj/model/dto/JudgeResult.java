package com.oj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 编程题评测结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResult {
    /**
     * 评测状态
     */
    private String status;
    
    /**
     * 执行时间(ms)
     */
    private Long executeTime;
    
    /**
     * 内存占用(KB)
     */
    private Long memoryUsage;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 测试用例结果
     */
    private List<TestcaseResult> testcaseResults;
    
    /**
     * 通过的测试用例数量
     */
    private Integer passedCount;
    
    /**
     * 总测试用例数量
     */
    private Integer totalCount;
} 