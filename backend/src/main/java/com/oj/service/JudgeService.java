package com.oj.service;

import com.oj.model.dto.JudgeResult;

/**
 * 编程题评测服务
 */
public interface JudgeService {
    /**
     * 提交评测任务
     * @param submissionId 提交记录ID
     */
    void submitJudgeTask(Long submissionId);
    
    /**
     * 执行评测
     * @param submissionId 提交记录ID
     */
    void judge(Long submissionId);
    
    /**
     * 保存评测结果
     * @param submissionId 提交记录ID
     * @param result 评测结果
     */
    void saveJudgeResult(Long submissionId, JudgeResult result);
} 