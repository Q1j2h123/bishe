package com.oj.model.vo.dashboard;

import lombok.Data;

import java.util.List;

/**
 * 提交统计VO
 */
@Data
public class SubmissionStatsVO {
    /**
     * 总提交数
     */
    private Integer totalSubmissions;
    
    /**
     * 通过的提交数
     */
    private Integer acceptedSubmissions;
    
    /**
     * 时间分布（最近7天每天的提交数）
     */
    private List<TimeDistribution> timeDistribution;
    
    /**
     * 语言使用分布
     */
    private List<LanguageDistribution> languageDistribution;
    
    @Data
    public static class TimeDistribution {
        /**
         * 日期
         */
        private String date;
        
        /**
         * 提交数
         */
        private Integer count;
    }
    
    @Data
    public static class LanguageDistribution {
        /**
         * 编程语言
         */
        private String language;
        
        /**
         * 提交数
         */
        private Integer count;
    }
} 