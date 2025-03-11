package com.oj.model.vo.dashboard;

import lombok.Data;

/**
 * 仪表盘统计信息VO
 */
@Data
public class DashboardStatsVO {
    /**
     * 题目总数
     */
    private Integer problemCount;
    
    /**
     * 用户总数
     */
    private Integer userCount;
    
    /**
     * 提交总数
     */
    private Integer submissionCount;
    
    /**
     * 通过率
     */
    private Double passRate;
    
    /**
     * 今日活跃用户数
     */
    private Integer todayActiveUsers;
    
    /**
     * 本周新增用户数
     */
    private Integer weeklyNewUsers;
    
    /**
     * 题目类型分布
     */
    private ProblemDistribution problemDistribution;
    
    /**
     * 题目难度分布
     */
    private DifficultyDistribution difficultyDistribution;
    
    @Data
    public static class ProblemDistribution {
        /**
         * 选择题数量
         */
        private Integer choice;
        
        /**
         * 判断题数量
         */
        private Integer judge;
        
        /**
         * 编程题数量
         */
        private Integer program;
    }
    
    @Data
    public static class DifficultyDistribution {
        /**
         * 简单题目数量
         */
        private Integer easy;
        
        /**
         * 中等题目数量
         */
        private Integer medium;
        
        /**
         * 困难题目数量
         */
        private Integer hard;
    }
} 