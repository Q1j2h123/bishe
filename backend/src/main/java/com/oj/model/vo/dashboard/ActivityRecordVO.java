package com.oj.model.vo.dashboard;

import lombok.Data;

import java.util.Date;

/**
 * 活动记录VO
 */
@Data
public class ActivityRecordVO {
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 操作类型
     * add - 添加
     * update - 更新
     * delete - 删除
     * submit - 提交
     * login - 登录
     * register - 注册
     */
    private String action;
    
    /**
     * 目标类型
     * 题目、系统等
     */
    private String targetType;
    
    /**
     * 目标ID
     */
    private Long targetId;
    
    /**
     * 目标名称
     */
    private String targetName;
    
    /**
     * 活动时间
     */
    private Date time;
    
    /**
     * IP地址
     */
    private String ip;
} 