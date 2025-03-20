package com.oj.config;

import com.oj.model.dto.ExecutionResult;
import com.oj.service.DeepseekService;
import com.oj.service.OpenAIService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 测试环境配置
 * 提供AI服务的模拟实现，避免测试依赖外部API
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public DeepseekService mockDeepseekService() {
        return new DeepseekService() {
            @Override
            public boolean validateCodeSyntax(String code, String language) {
                return true;  // 假设测试中的代码总是合法的
            }

            @Override
            public String getCompileErrorMessage(String code, String language) {
                return "测试用编译错误信息";
            }

            @Override
            public ExecutionResult evaluateCode(String compiledCode, String input, String language) {
                return ExecutionResult.builder()
                        .success(true)
                        .output("测试输出")
                        .executeTime(100L)
                        .memoryUsage(10240L)
                        .build();
            }
        };
    }

    @Bean
    @Primary
    public OpenAIService mockOpenAIService() {
        return new OpenAIService() {
            @Override
            public boolean validateCodeSyntax(String code, String language) {
                return true;  // 假设测试中的代码总是合法的
            }

            @Override
            public String getCompileErrorMessage(String code, String language) {
                return "测试用编译错误信息";
            }

            @Override
            public ExecutionResult evaluateCode(String compiledCode, String input, String language) {
                return ExecutionResult.builder()
                        .success(true)
                        .output("测试输出")
                        .executeTime(100L)
                        .memoryUsage(10240L)
                        .build();
            }
        };
    }
} 