package com.oj.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserListVO {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 用户账号
     */
    private String userAccount;
    
    /**
     * 用户角色
     */
    private String userRole;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 