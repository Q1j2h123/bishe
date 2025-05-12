package com.oj.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 令牌黑名单服务
 * 用于管理已经失效的JWT令牌
 */
@Service
public class TokenBlacklistService {
    
    // 使用线程安全的集合存储黑名单
    private Set<String> blacklist = ConcurrentHashMap.newKeySet();
    
    /**
     * 将令牌添加到黑名单
     * @param token JWT令牌
     */
    public void addToBlacklist(String token) {
        blacklist.add(token);
    }
    
    /**
     * 检查令牌是否在黑名单中
     * @param token JWT令牌
     * @return 如果令牌在黑名单中，返回true
     */
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
    
    /**
     * 从黑名单中移除令牌（可选，用于清理过期令牌）
     * @param token JWT令牌
     */
    public void removeFromBlacklist(String token) {
        blacklist.remove(token);
    }
    
    /**
     * 获取黑名单大小
     * @return 黑名单中的令牌数量
     */
    public int getBlacklistSize() {
        return blacklist.size();
    }
    
    /**
     * 获取所有黑名单令牌
     * @return 包含所有黑名单令牌的集合
     */
    public Set<String> getAllBlacklistedTokens() {
        // 返回一个新的集合，避免外部直接修改内部集合
        return new HashSet<>(blacklist);
    }
    
    /**
     * 清空黑名单
     */
    public void clearBlacklist() {
        blacklist.clear();
    }
} 