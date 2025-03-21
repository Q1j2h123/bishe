package com.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.entity.Submission;
import com.oj.model.request.ChoiceJudgeSubmissionRequest;
import com.oj.model.request.ProgramSubmissionRequest;
import com.oj.model.vo.ChoiceJudgeSubmissionListVO;
import com.oj.model.vo.ProgramSubmissionListVO;
import com.oj.model.vo.SubmissionListVO;
import com.oj.model.entity.ChoiceJudgeSubmission;
import com.oj.model.entity.ProgramSubmission;
import com.oj.model.vo.ChoiceJudgeSubmissionDetailVO;
import com.oj.model.vo.ProgramSubmissionDetailVO;
import com.oj.model.vo.SubmissionDetailVO;

public interface SubmissionService extends IService<Submission> {

    /**
     * 提交选择题或判断题答案
     * @param userId 用户ID
     * @param request 提交请求
     * @return 提交ID
     */
    Long submitChoiceJudgeAnswer(Long userId, ChoiceJudgeSubmissionRequest request);

    /**
     * 提交编程题代码
     * @param userId 用户ID
     * @param request 提交请求
     * @return 提交ID
     */
    Long submitProgramCode(Long userId, ProgramSubmissionRequest request);


    Page<SubmissionListVO> getUserSubmissions(Long userId, long current, long size, 
                                            String type, String status, String difficulty, 
                                            String jobType, String tag, String keyword);

    /**
     * 获取题目的提交列表
     * @param problemId 题目ID
     * @param userId 用户ID（必选，指定用户的提交记录）
     * @param current 当前页
     * @param size 每页数量
     * @return 提交列表
     */
    Page<SubmissionListVO> getProblemSubmissions(Long problemId, Long userId, long current, long size);

    /**
     * 获取提交详情
     * @param submissionId 提交ID
     * @param userId 用户ID（用于权限校验和特殊字段展示判断）
     * @return 提交详情
     */
    SubmissionDetailVO getSubmissionDetail(Long submissionId, Long userId);
    
    /**
     * 获取选择/判断题提交详情
     * @param submissionId 提交ID
     * @param userId 用户ID
     * @return 选择/判断题提交详情
     */
    ChoiceJudgeSubmissionDetailVO getChoiceJudgeSubmissionDetail(Long submissionId, Long userId);
    
    /**
     * 获取编程题提交详情
     * @param submissionId 提交ID
     * @param userId 用户ID
     * @return 编程题提交详情
     */
    ProgramSubmissionDetailVO getProgramSubmissionDetail(Long submissionId, Long userId);
    
    /**
     * 更新用户题目状态
     * @param userId 用户ID
     * @param problemId 题目ID
     * @param status 状态
     */
    void updateUserProblemStatus(Long userId, Long problemId, String status);
    
    /**
     * 获取题目的提交次数
     * @param problemId 题目ID
     * @return 提交次数
     */
    Long countProblemSubmissions(Long problemId);
    
    /**
     * 获取用户的提交次数
     * @param userId 用户ID
     * @return 提交次数
     */
    Long countUserSubmissions(Long userId);

    /**
     * 获取用户最近提交
     */
    Page<SubmissionListVO> getRecentSubmissionsByUserId(long userId, long pageNum, long pageSize);

    /**
     * 统计提交总数
     */
    Integer countSubmissions();
}