package com.oj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.dto.SubmissionRequest;
import com.oj.model.entity.ChoiceJudgeSubmission;
import java.util.List;

/**
 * 选择判断题提交记录服务
 */
public interface ChoiceJudgeSubmissionService extends IService<ChoiceJudgeSubmission> {
    
    /**
     * 提交选择判断题答案
     */
    String submit(SubmissionRequest submissionRequest);

    /**
     * 获取用户某题目的所有提交记录
     */
    List<ChoiceJudgeSubmission> getUserProblemSubmissions(Long userId, Long problemId);

    /**
     * 获取用户的所有提交记录
     */
    List<ChoiceJudgeSubmission> getUserSubmissions(Long userId);

    /**
     * 获取题目的通过率
     */
    String getProblemAcceptRate(Long problemId);
} 