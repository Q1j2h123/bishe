package com.oj.config;

import com.oj.service.CodeExecutor;
import com.oj.service.impl.AICodeExecutor;
import com.oj.service.impl.DockerCodeExecutor;
import com.oj.service.impl.MockCodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 评测系统配置
 */
@Configuration
@Slf4j
public class JudgeConfig {
    
    @Value("${judge.enable:true}")
    private boolean judgeEnable;
    
    @Value("${judge.use-mock-executor:false}")
    private boolean useMockExecutor;
    
    @Value("${judge.use-ai-executor:false}")
    private boolean useAIExecutor;
    
    /**
     * 配置代码执行器
     * 根据配置选择使用模拟执行器、AI执行器还是Docker执行器
     */
    @Bean
    @Primary
    public CodeExecutor codeExecutor(DockerCodeExecutor dockerCodeExecutor, 
                                     MockCodeExecutor mockCodeExecutor,
                                     AICodeExecutor aiCodeExecutor) {
        if (!judgeEnable) {
            log.info("评测功能已禁用");
            return mockCodeExecutor;
        }
        
        if (useMockExecutor) {
            log.info("使用模拟代码执行器");
            return mockCodeExecutor;
        }
        
        if (useAIExecutor) {
            log.info("使用AI代码执行器");
            return aiCodeExecutor;
        }
        
        if (dockerCodeExecutor.isDockerAvailable()) {
            log.info("使用Docker代码执行器");
            return dockerCodeExecutor;
        } else {
            log.warn("Docker不可用，自动降级使用模拟执行器");
            return mockCodeExecutor;
        }
    }
} 