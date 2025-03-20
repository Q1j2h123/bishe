package com.oj.service.impl;

import com.oj.config.TestConfig;
import com.oj.service.DeepseekService;
import com.oj.service.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI服务集成测试
 * 导入TestConfig以提供AI服务的模拟实现
 */
@SpringBootTest
@Slf4j
@Import(TestConfig.class)
public class AIServiceTest {

    @Autowired
    private DeepseekService deepseekService;

    @Autowired
    private OpenAIService openAIService;

    @Value("${ai.primary-service:deepseek}")
    private String primaryService;

    @Test
    public void testAIServices() {
        log.info("=== AI服务测试 ===");
        assertNotNull(deepseekService, "Deepseek服务不应为空");
        assertNotNull(openAIService, "OpenAI服务不应为空");
        
        // 测试Deepseek服务
        boolean deepseekResult = deepseekService.validateCodeSyntax(
            "public class Test { public static void main(String[] args) { System.out.println(\"Hello\"); } }", 
            "java"
        );
        assertTrue(deepseekResult, "代码应该验证通过");
        
        // 测试OpenAI服务
        boolean openaiResult = openAIService.validateCodeSyntax(
            "public class Test { public static void main(String[] args) { System.out.println(\"Hello\"); } }", 
            "java"
        );
        assertTrue(openaiResult, "代码应该验证通过");
        
        log.info("AI服务测试通过");
    }
} 