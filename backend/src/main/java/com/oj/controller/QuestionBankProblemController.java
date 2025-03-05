package com.oj.controller;

import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.exception.BusinessException;
import com.oj.model.entity.User;
import com.oj.service.QuestionBankProblemService;
import com.oj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题库题目关联接口
 */
@RestController
@RequestMapping("/questionBank/problem")
@Slf4j
public class QuestionBankProblemController {

    @Resource
    private QuestionBankProblemService questionBankProblemService;

    @Resource
    private UserService userService;

    /**
     * 添加题目到题库
     *
     * @param questionBankId 题库 id
     * @param problemId 题目 id
     * @param request HTTP 请求
     * @return 是否添加成功
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addProblemToBank(@RequestParam long questionBankId,
                                                @RequestParam long problemId,
                                                HttpServletRequest request) {
        if (questionBankId <= 0 || problemId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 添加题目到题库
        boolean result = questionBankProblemService.addProblemToBank(questionBankId, problemId);
        return ResultUtils.success(result);
    }

    /**
     * 从题库中移除题目
     *
     * @param questionBankId 题库 id
     * @param problemId 题目 id
     * @param request HTTP 请求
     * @return 是否移除成功
     */
    @PostMapping("/remove")
    public BaseResponse<Boolean> removeProblemFromBank(@RequestParam long questionBankId,
                                                     @RequestParam long problemId,
                                                     HttpServletRequest request) {
        if (questionBankId <= 0 || problemId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 从题库中移除题目
        boolean result = questionBankProblemService.removeProblemFromBank(questionBankId, problemId);
        return ResultUtils.success(result);
    }

    /**
     * 获取题库中的所有题目 id
     *
     * @param questionBankId 题库 id
     * @return 题目 id 列表
     */
    @GetMapping("/list")
    public BaseResponse<List<Long>> getProblemIdsByBankId(@RequestParam long questionBankId) {
        if (questionBankId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<Long> problemIds = questionBankProblemService.getProblemIdsByBankId(questionBankId);
        return ResultUtils.success(problemIds);
    }

    /**
     * 批量添加题目到题库
     *
     * @param questionBankId 题库 id
     * @param problemIds 题目 id 列表
     * @param request HTTP 请求
     * @return 是否添加成功
     */
    @PostMapping("/batch/add")
    public BaseResponse<Boolean> batchAddProblemsToBank(@RequestParam long questionBankId,
                                                       @RequestBody List<Long> problemIds,
                                                       HttpServletRequest request) {
        if (questionBankId <= 0 || problemIds == null || problemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 批量添加题目到题库
        boolean result = questionBankProblemService.batchAddProblemsToBank(questionBankId, problemIds);
        return ResultUtils.success(result);
    }

    /**
     * 批量从题库中移除题目
     *
     * @param questionBankId 题库 id
     * @param problemIds 题目 id 列表
     * @param request HTTP 请求
     * @return 是否移除成功
     */
    @PostMapping("/batch/remove")
    public BaseResponse<Boolean> batchRemoveProblemsFromBank(@RequestParam long questionBankId,
                                                            @RequestBody List<Long> problemIds,
                                                            HttpServletRequest request) {
        if (questionBankId <= 0 || problemIds == null || problemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 批量从题库中移除题目
        boolean result = questionBankProblemService.batchRemoveProblemsFromBank(questionBankId, problemIds);
        return ResultUtils.success(result);
    }
} 