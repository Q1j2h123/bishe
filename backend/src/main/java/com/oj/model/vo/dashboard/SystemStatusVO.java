package com.oj.model.vo.dashboard;

import lombok.Data;

/**
 * 系统状态VO
 */
@Data
public class SystemStatusVO {
    /**
     * CPU使用率
     */
    private Double cpuUsage;
    
    /**
     * 内存使用率
     */
    private Double memoryUsage;
    
    /**
     * 磁盘使用率
     */
    private Double diskUsage;
    
    /**
     * 平均响应时间(ms)
     */
    private Integer averageResponseTime;
    
    /**
     * 在线用户数
     */
    private Integer onlineUsers;
    
    /**
     * 判题服务器状态
     * up - 正常
     * down - 宕机
     * degraded - 服务降级
     */
    private String judgeServerStatus;
} 