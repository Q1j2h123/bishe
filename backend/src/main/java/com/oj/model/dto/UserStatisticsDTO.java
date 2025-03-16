package com.oj.model.dto;

import lombok.Data;
import java.util.Map;

/**
 * 用户统计数据DTO
 */
@Data
public class UserStatisticsDTO {
    /**
     * 已解决题目总数
     */
    private Integer totalSolved;
    
    /**
     * 总提交次数
     */
    private Integer totalSubmissions;
    
    /**
     * 正确率
     */
    private String correctRate;
    
    /**
     * 连续解题天数
     */
    private Integer streak;
    
    /**
     * 题目类型统计
     */
    private Map<String, TypeCount> typeCounts;
    
    /**
     * 题目难度统计
     */
    private Map<String, TypeCount> difficultyCounts;
    
    /**
     * 类型或难度统计
     */
    @Data
    public static class TypeCount {
        /**
         * 总数
         */
        private Integer total;
        
        /**
         * 已解决数
         */
        private Integer solved;
    }
}