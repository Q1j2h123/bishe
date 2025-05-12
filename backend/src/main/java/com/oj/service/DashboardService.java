package com.oj.service;

import com.oj.model.vo.dashboard.*;

/**
 * 控制面板服务
 */
public interface DashboardService {

    /**
     * 获取控制面板统计数据
     * @return 统计数据VO
     */
    DashboardStatsVO getStats();

    /**
     * 获取最近活动记录
     * @param limit 获取条数
     * @return 活动记录VO数组
     */
    ActivityRecordVO[] getRecentActivities(Integer limit);

    /**
     * 获取提交统计
     * @return 提交统计VO
     */
    SubmissionStatsVO getSubmissionStats();
} 