package com.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.common.BaseResponse;
import com.oj.common.ResultUtils;
import com.oj.common.UserContext;
import com.oj.exception.BusinessException;
import com.oj.common.ErrorCode;
import com.oj.model.entity.User;
import com.oj.model.request.ChoiceJudgeSubmissionRequest;
import com.oj.model.request.ProgramSubmissionRequest;
import com.oj.model.vo.ChoiceJudgeSubmissionDetailVO;
import com.oj.model.vo.ProgramSubmissionDetailVO;
import com.oj.model.vo.SubmissionDetailVO;
import com.oj.model.vo.SubmissionListVO;
import com.oj.service.SubmissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 提交控制器
 */
@RestController
@RequestMapping("/api/submission")
public class SubmissionController {

    @Resource
    private SubmissionService submissionService;

    /**
     * 提交选择/判断题答案
     *
     * @param request 提交请求
     * @return 提交ID
     */
    @PostMapping("/choice-judge")
    public BaseResponse<Long> submitChoiceJudgeAnswer(@RequestBody ChoiceJudgeSubmissionRequest request) {
        // 从当前登录用户信息中获取用户ID
        User loginUser = UserContext.getUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = loginUser.getId();
        Long submissionId = submissionService.submitChoiceJudgeAnswer(userId, request);
        return ResultUtils.success(submissionId);
    }

    /**
     * 提交编程题代码
     *
     * @param request 提交请求
     * @return 提交ID
     */
    @PostMapping("/program")
    public BaseResponse<Long> submitProgramCode(@RequestBody ProgramSubmissionRequest request) {
        // 从当前登录用户信息中获取用户ID
        User loginUser = UserContext.getUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = loginUser.getId();
        Long submissionId = submissionService.submitProgramCode(userId, request);
        return ResultUtils.success(submissionId);
    }

    /**
     * 获取用户的提交列表
     *
     * @param userId  用户ID（可选，不传则查询当前登录用户）
     * @param current 当前页码
     * @param size    每页记录数
     * @param type    类型
     * @param status  状态
     * @param difficulty 难度
     * @param jobType 岗位类型
     * @param tag     标签
     * @param tagList 多标签查询参数
     * @param keyword 关键词
     * @return 提交列表
     */
    @GetMapping("/user/list")
    public BaseResponse<Page<SubmissionListVO>> getUserSubmissions(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String tagList,
            @RequestParam(required = false) String keyword) {
        // 如果userId为空，则使用当前登录用户ID
        if (userId == null) {
            User loginUser = UserContext.getUser();
            if (loginUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            userId = loginUser.getId();
        }
        
        // 处理多标签查询
        String tagQuery = tag;
        if (StringUtils.isNotBlank(tagList)) {
            // 如果提供了tagList，则使用tagList作为标签查询参数
            tagQuery = tagList;
        }
        
        Page<SubmissionListVO> submissionListVOPage = submissionService.getUserSubmissions(
                userId, current, size, type, status, difficulty, jobType, tagQuery, keyword);
        return ResultUtils.success(submissionListVOPage);
    }

    /**
     * 获取题目的提交列表
     *
     * @param problemId 题目ID
     * @param userId 用户ID（当前登录用户或指定用户）
     * @param current   当前页码
     * @param size      每页记录数
     * @return 提交列表
     */
    @GetMapping("/problem/list")
    public BaseResponse<Page<SubmissionListVO>> getProblemSubmissions(
            @RequestParam Long problemId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        // 如果userId为空，使用当前登录用户的ID
        if (userId == null) {
            User loginUser = UserContext.getUser();
            if (loginUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            userId = loginUser.getId();
        }

        Page<SubmissionListVO> submissionListVOPage = submissionService.getProblemSubmissions(problemId, userId, current, size);
        return ResultUtils.success(submissionListVOPage);
    }

    /**
     * 获取提交详情
     *
     * @param submissionId 提交ID
     * @return 提交详情
     */
    @GetMapping("/detail")
    public BaseResponse<SubmissionDetailVO> getSubmissionDetail(@RequestParam Long submissionId) {
        // 从当前登录用户信息中获取用户ID
        User loginUser = UserContext.getUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        Long userId = loginUser.getId();
        SubmissionDetailVO submissionDetailVO = submissionService.getSubmissionDetail(submissionId, userId);
        return ResultUtils.success(submissionDetailVO);
    }

    /**
     * 获取选择/判断题提交详情
     *
     * @param submissionId 提交ID
     * @return 选择/判断题提交详情
     */
    @GetMapping("/detail/choice-judge")
    public BaseResponse<ChoiceJudgeSubmissionDetailVO> getChoiceJudgeSubmissionDetail(@RequestParam Long submissionId) {
        // 从当前登录用户信息中获取用户ID
        User loginUser = UserContext.getUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = loginUser.getId();
        ChoiceJudgeSubmissionDetailVO detailVO = submissionService.getChoiceJudgeSubmissionDetail(submissionId, userId);
        return ResultUtils.success(detailVO);
    }

    /**
     * 获取编程题提交详情
     *
     * @param submissionId 提交ID
     * @return 编程题提交详情
     */
    @GetMapping("/detail/program")
    public BaseResponse<ProgramSubmissionDetailVO> getProgramSubmissionDetail(@RequestParam Long submissionId) {
        // 从当前登录用户信息中获取用户ID
        User loginUser = UserContext.getUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = loginUser.getId();
        ProgramSubmissionDetailVO detailVO = submissionService.getProgramSubmissionDetail(submissionId, userId);
        return ResultUtils.success(detailVO);
    }

    /**
     * 统计题目的提交次数
     *
     * @param problemId 题目ID
     * @return 提交次数
     */
    @GetMapping("/count/problem")
    public BaseResponse<Long> countProblemSubmissions(@RequestParam Long problemId) {
        Long count = submissionService.countProblemSubmissions(problemId);
        return ResultUtils.success(count);
    }

    /**
     * 统计用户的提交次数
     *
     * @param userId 用户ID（可选，不传则查询当前登录用户）
     * @return 提交次数
     */
    @GetMapping("/count/user")
    public BaseResponse<Long> countUserSubmissions(@RequestParam(required = false) Long userId) {
        // 如果userId为空，则使用当前登录用户ID
        if (userId == null) {
            User loginUser = UserContext.getUser();
            if (loginUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            userId = loginUser.getId();
        }
        Long count = submissionService.countUserSubmissions(userId);
        return ResultUtils.success(count);
    }
} 