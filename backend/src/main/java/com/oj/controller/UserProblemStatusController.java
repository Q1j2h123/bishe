package com.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.common.BaseResponse;
import com.oj.common.ResultUtils;
import com.oj.model.entity.User;
import com.oj.model.entity.UserProblemStatus;
import com.oj.model.vo.UserProblemStatusVO;
import com.oj.service.UserProblemStatusService;
import com.oj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户题目状态接口
 */
@RestController
@RequestMapping("/api/user/problem-status")
@Slf4j
public class UserProblemStatusController {
    
    @Resource
    private UserProblemStatusService userProblemStatusService;
    
    @Resource
    private UserService userService;
    /**
     * 批量获取题目状态 - 支持GET和POST请求
     * @param //request 请求参数，包含problemIds数组
     * @param httpServletRequest HTTP请求
     * @return 题目ID-状态映射
     */
    @GetMapping("/batch")
    public BaseResponse<Map<Long, String>> getBatchProblemStatusGet(
            @RequestParam List<Long> problemIds,
            HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Map<Long, String> statusMap = userProblemStatusService.getBatchProblemStatus(
                loginUser.getId(), problemIds);
        return ResultUtils.success(statusMap);
    }

    /**
     * 批量获取题目状态 - POST请求版本
     * @param request 请求参数，包含problemIds数组
     * @param httpServletRequest HTTP请求
     * @return 题目ID-状态映射
     */
    @PostMapping("/batch")
    public BaseResponse<Map<Long, String>> getBatchProblemStatusPost(
            @RequestBody Map<String, List<Long>> request,
            HttpServletRequest httpServletRequest) {
        List<Long> problemIds = request.get("problemIds");
        if (problemIds == null || problemIds.isEmpty()) {
            return ResultUtils.success(Collections.emptyMap());
        }
        
        User loginUser = userService.getLoginUser(httpServletRequest);
        Map<Long, String> statusMap = userProblemStatusService.getBatchProblemStatus(
                loginUser.getId(), problemIds);
        return ResultUtils.success(statusMap);
    }
    /**
     * 获取用户题目状态
     * @param problemId 题目ID
     * @param httpServletRequest HTTP请求
     * @return 用户题目状态
     */
    @GetMapping("/get")
    public BaseResponse<UserProblemStatus> getUserProblemStatus(@RequestParam Long problemId, 
                                                               HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        UserProblemStatus status = userProblemStatusService.getUserProblemStatus(loginUser.getId(), problemId);
        return ResultUtils.success(status);
    }
    
    /**
     * 获取用户已解决的题目列表
     * @param httpServletRequest HTTP请求
     * @return 已解决题目状态列表
     */
    @GetMapping("/solved")
    public BaseResponse<List<UserProblemStatusVO>> getUserSolvedProblems(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        List<UserProblemStatusVO> solvedProblems = userProblemStatusService.getUserSolvedProblems(loginUser.getId());
        return ResultUtils.success(solvedProblems);
    }
    
    /**
     * 获取用户尝试过的题目列表
     * @param httpServletRequest HTTP请求
     * @return 尝试过题目状态列表
     */
    @GetMapping("/attempted")
    public BaseResponse<List<UserProblemStatusVO>> getUserAttemptedProblems(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        List<UserProblemStatusVO> attemptedProblems = userProblemStatusService.getUserAttemptedProblems(loginUser.getId());
        return ResultUtils.success(attemptedProblems);
    }
    
    /**
     * 获取用户已解决的题目数量
     * @param httpServletRequest HTTP请求
     * @return 已解决题目数量
     */
    @GetMapping("/count/solved")
    public BaseResponse<Long> countUserSolvedProblems(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Long count = userProblemStatusService.countUserSolvedProblems(loginUser.getId());
        return ResultUtils.success(count);
    }
    
    /**
     * 获取用户尝试过的题目数量
     * @param httpServletRequest HTTP请求
     * @return 尝试过题目数量
     */
    @GetMapping("/count/attempted")
    public BaseResponse<Long> countUserAttemptedProblems(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Long count = userProblemStatusService.countUserAttemptedProblems(loginUser.getId());
        return ResultUtils.success(count);
    }
    
    /**
     * 分页获取用户题目状态列表
     * @param status 状态，可为空
     * @param current 当前页
     * @param size 每页大小
     * @param httpServletRequest HTTP请求
     * @return 分页结果
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<UserProblemStatusVO>> getUserProblemStatusList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Page<UserProblemStatusVO> page = userProblemStatusService.getUserProblemStatusList(
                loginUser.getId(), status, current, size);
        return ResultUtils.success(page);
    }
} 