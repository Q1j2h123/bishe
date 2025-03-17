package com.oj.controller;

import com.oj.annotation.AuthCheck;
import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.common.UserContext;
import com.oj.model.entity.User;
import com.oj.model.vo.dashboard.ActivityRecordVO;
import com.oj.model.vo.dashboard.DashboardStatsVO;
import com.oj.model.vo.dashboard.SubmissionStatsVO;
import com.oj.service.DashboardService;
import com.oj.service.ProblemService;
import com.oj.service.SubmissionService;
import com.oj.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/api")
@Api(tags = "仪表盘接口")
@Slf4j
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @Resource
    private ProblemService problemService;

    @Resource
    private UserService userService;

    @Resource
    private SubmissionService submissionService;

    /**
     * 获取首页统计数据
     */
    @GetMapping("/dashboard/home/stats")
    @ApiOperation(value = "获取首页统计数据", notes = "返回题目总数、用户总数、提交总数")
    public BaseResponse<Map<String, Object>> getHomeStats() {
        try {
            Map<String, Object> statsMap = new HashMap<>();
            
            // 获取真实数据
            Integer problemCount = problemService.countProblems();
            Integer userCount = userService.countUsers();
            Integer submissionCount = submissionService.countSubmissions();
            
            statsMap.put("totalProblems", problemCount);
            statsMap.put("totalUsers", userCount);
            statsMap.put("totalSubmissions", submissionCount);
            
            return ResultUtils.success(statsMap);
        } catch (Exception e) {
            log.error("获取首页统计数据出错", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取首页统计数据失败");
        }
    }
    
    /**
     * 获取管理员控制面板统计数据
     */
    @GetMapping("/admin/dashboard/stats")
    @AuthCheck(mustRole = "admin")
    @ApiOperation(value = "获取控制面板统计数据", notes = "返回详细的统计数据")
    public BaseResponse<DashboardStatsVO> getAdminStats() {
        try {
            // 获取当前用户信息并验证管理员权限
            User user = UserContext.getUser();
            if (user == null || !"admin".equals(user.getUserRole())) {
                return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
            }
            
            DashboardStatsVO stats = dashboardService.getStats();
            return ResultUtils.success(stats);
        } catch (Exception e) {
            log.error("获取控制面板统计数据出错", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取控制面板统计数据失败");
        }
    }
    
    /**
     * 获取提交统计
     */
    @GetMapping("/admin/dashboard/submissions")
    @AuthCheck(mustRole = "admin")
    @ApiOperation(value = "获取提交统计", notes = "返回提交相关的统计数据")
    public BaseResponse<SubmissionStatsVO> getSubmissionStats() {
        try {
            // 获取当前用户信息并验证管理员权限
            User user = UserContext.getUser();
            if (user == null || !"admin".equals(user.getUserRole())) {
                return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
            }
            
            SubmissionStatsVO submissionStats = dashboardService.getSubmissionStats();
            return ResultUtils.success(submissionStats);
        } catch (Exception e) {
            log.error("获取提交统计出错", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取提交统计失败");
        }
    }
    
    /**
     * 获取最近活动
     */
    @GetMapping("/admin/dashboard/activities")
    @AuthCheck(mustRole = "admin")
    @ApiOperation(value = "获取最近活动", notes = "返回最近的活动记录")
    public BaseResponse<ActivityRecordVO[]> getRecentActivities(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            // 获取当前用户信息并验证管理员权限
            User user = UserContext.getUser();
            if (user == null || !"admin".equals(user.getUserRole())) {
                return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
            }
            
            ActivityRecordVO[] activities = dashboardService.getRecentActivities(limit);
            return ResultUtils.success(activities);
        } catch (Exception e) {
            log.error("获取最近活动出错", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取最近活动失败");
        }
    }
} 