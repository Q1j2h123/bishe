package com.oj.service;

import com.oj.model.dto.UserStatisticsDTO;

/**
 * 用户统计服务接口
 */
public interface UserStatsService {
    
    /**
     * 获取用户统计数据
     * @param userId 用户ID
     * @return 用户统计数据
     */
    UserStatisticsDTO getUserStatistics(Long userId);
} 