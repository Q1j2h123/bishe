package com.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.entity.UserProblemStatus;
import com.oj.model.vo.UserProblemStatusVO;

import java.util.List;
import java.util.Map;

/**
 * 用户题目状态服务接口
 */
public interface UserProblemStatusService extends IService<UserProblemStatus> {
    
    /**
     * 获取用户的题目状态
     * @param userId 用户ID
     * @param problemId 题目ID
     * @return 用户题目状态
     */
    UserProblemStatus getUserProblemStatus(Long userId, Long problemId);
    /**
     * 批量获取题目状态
     * @param userId 用户ID
     * @param problemIds 题目ID列表
     * @return 题目ID-状态映射
     */
    Map<Long, String> getBatchProblemStatus(Long userId, List<Long> problemIds);
    /**
     * 获取用户已解决的题目列表
     * @param userId 用户ID
     * @return 已解决题目状态列表
     */
    List<UserProblemStatusVO> getUserSolvedProblems(Long userId);
    
    /**
     * 获取用户尝试过的题目列表
     * @param userId 用户ID
     * @return 尝试过题目状态列表
     */
    List<UserProblemStatusVO> getUserAttemptedProblems(Long userId);
    
    /**
     * 获取用户已解决的题目数量
     * @param userId 用户ID
     * @return 已解决题目数量
     */
    Long countUserSolvedProblems(Long userId);
    
    /**
     * 获取用户尝试过的题目数量
     * @param userId 用户ID
     * @return 尝试过题目数量
     */
    Long countUserAttemptedProblems(Long userId);
    
    /**
     * 分页获取用户题目状态列表
     * @param userId 用户ID
     * @param status 状态，可为空
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    Page<UserProblemStatusVO> getUserProblemStatusList(Long userId, String status, long current, long size);
    
    /**
     * 强制更新用户题目状态，不考虑"只升不降"规则
     * @param userId 用户ID
     * @param problemId 题目ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean forceUpdateStatus(Long userId, Long problemId, String status);
} 