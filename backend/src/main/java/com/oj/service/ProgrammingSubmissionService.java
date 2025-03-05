package com.oj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.dto.SubmissionRequest;
import com.oj.model.entity.ProgrammingSubmission;
import java.util.List;

/**
 * 编程题提交记录服务
 */
public interface ProgrammingSubmissionService extends IService<ProgrammingSubmission> {
    
    /**
     * 提交编程题答案
     */
    String submit(SubmissionRequest submissionRequest);

    /**
     * 获取用户某题目的所有提交记录
     */
    List<ProgrammingSubmission> getUserProblemSubmissions(Long userId, Long problemId);

    /**
     * 获取用户的所有提交记录
     */
    List<ProgrammingSubmission> getUserSubmissions(Long userId);

    /**
     * 获取题目的通过率
     */
    String getProblemAcceptRate(Long problemId);
} 