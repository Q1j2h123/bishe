package com.oj.utils;

import org.springframework.stereotype.Component;

/**
 * 用户信息持有者（线程本地存储）
 */
@Component
public class UserHolder {
    private final ThreadLocal<Long> userIdThreadLocal = new ThreadLocal<>();
    
    /**
     * 设置当前用户ID
     */
    public void setUserId(Long userId) {
        userIdThreadLocal.set(userId);
    }
    
    /**
     * 获取当前用户ID
     */
    public Long getUserId() {
        return userIdThreadLocal.get();
    }
    
    /**
     * 清除当前用户ID
     */
    public void removeUserId() {
        userIdThreadLocal.remove();
    }
} 