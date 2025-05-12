package com.oj.controller;

import com.oj.common.BaseResponse;
import com.oj.common.ResultUtils;
import com.oj.model.dto.UserStatisticsDTO;
import com.oj.model.entity.User;
import com.oj.service.UserService;
import com.oj.service.UserStatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户统计数据接口
 */
@RestController
@RequestMapping("/api/user")
@Api(tags = "用户统计接口")
@Slf4j
public class UserStatsController {

    @Resource
    private UserStatsService userStatsService;

    @Resource
    private UserService userService;

    /**
     * 获取用户统计数据
     * @param httpServletRequest HTTP请求
     * @return 用户统计数据
     */
    @GetMapping("/statistics")
    @ApiOperation(value = "获取用户统计数据", notes = "获取用户的解题统计、类型分布、难度分布等数据")
    public BaseResponse<UserStatisticsDTO> getUserStatistics(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        UserStatisticsDTO statistics = userStatsService.getUserStatistics(loginUser.getId());
        return ResultUtils.success(statistics);
    }
} 