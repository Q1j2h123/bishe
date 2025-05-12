package com.oj.model.dto;

import lombok.Data;

/**
 * 日历数据DTO
 */
@Data
public class CalendarDataDTO {
    /**
     * 日期
     */
    private String date;
    
    /**
     * 提交次数
     */
    private Integer count;
} 