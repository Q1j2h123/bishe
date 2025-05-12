package com.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.common.BaseResponse;
import com.oj.common.ResultUtils;
import com.oj.model.dto.ErrorProblemDTO;
import com.oj.model.entity.User;
import com.oj.model.request.ErrorProblemQueryRequest;
import com.oj.service.ErrorProblemService;
import com.oj.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 错题本接口
 */
@RestController
@RequestMapping("/api/user")
@Api(tags = "错题本接口")
@Slf4j
public class ErrorProblemController {

    @Resource
    private ErrorProblemService errorProblemService;

    @Resource
    private UserService userService;

    /**
     * 获取用户错题本
     *
     * @param errorProblemQueryRequest 查询条件
     * @param request HTTP请求
     * @return 分页错题列表
     */
    @GetMapping("/error-problems")
    @ApiOperation(value = "获取用户错题本", notes = "分页获取用户的错题记录")
    public BaseResponse<Page<ErrorProblemDTO>> getErrorProblems(ErrorProblemQueryRequest errorProblemQueryRequest, 
                                                            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ErrorProblemDTO> errorProblemPage = errorProblemService.getErrorProblemsByUserId(
                loginUser.getId(), errorProblemQueryRequest);
        return ResultUtils.success(errorProblemPage);
    }

    /**
     * 标记题目为已掌握
     *
     * @param problemId 题目ID
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/mark-mastered/{problemId}")
    @ApiOperation(value = "标记题目为已掌握", notes = "将错题标记为已掌握状态")
    public BaseResponse<Boolean> markProblemAsMastered(@PathVariable("problemId") Long problemId,
                                                   HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = errorProblemService.markAsMastered(loginUser.getId(), problemId);
        return ResultUtils.success(result);
    }
} 