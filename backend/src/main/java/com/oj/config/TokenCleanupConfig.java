package com.oj.config;

import com.oj.service.TokenBlacklistService;
import com.oj.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 令牌清理配置
 * 定期清理已过期的黑名单令牌
 */
@Configuration
@EnableScheduling
@Slf4j
public class TokenCleanupConfig {

    @Resource
    private TokenBlacklistService tokenBlacklistService;
    
    @Resource
    private JwtUtils jwtUtils;
    
    /**
     * 每天凌晨2点执行清理任务
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        log.info("开始清理过期的黑名单令牌...");
        
        // 由于TokenBlacklistService没有提供批量操作的方法
        // 这里我们创建一个新的方法来实现清理
        int beforeSize = tokenBlacklistService.getBlacklistSize();
        int cleaned = cleanupTokens();
        int afterSize = tokenBlacklistService.getBlacklistSize();
        
        log.info("黑名单令牌清理完成。清理前: {}, 清理后: {}, 清理数量: {}", 
                beforeSize, afterSize, cleaned);
    }
    
    /**
     * 清理过期的令牌
     * @return 清理的令牌数量
     */
    private int cleanupTokens() {
        // 获取所有黑名单令牌
        Set<String> tokensToCheck = tokenBlacklistService.getAllBlacklistedTokens();
        int cleanedCount = 0;
        
        // 遍历所有令牌，检查是否过期
        for (String token : tokensToCheck) {
            try {
                // 检查令牌是否过期
                if (!jwtUtils.validateToken(token)) {
                    // 如果令牌已过期，从黑名单中移除
                    tokenBlacklistService.removeFromBlacklist(token);
                    cleanedCount++;
                }
            } catch (Exception e) {
                // 如果验证时出现异常，也认为令牌无效，可以移除
                tokenBlacklistService.removeFromBlacklist(token);
                cleanedCount++;
                log.warn("清理黑名单令牌时发生异常: {}", e.getMessage());
            }
        }
        
        return cleanedCount;
    }
} 