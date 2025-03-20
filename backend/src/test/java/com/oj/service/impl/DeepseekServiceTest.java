package com.oj.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DeepseekService单元测试
 * 这是一个普通的JUnit测试，不加载Spring上下文
 */
@Slf4j
public class DeepseekServiceTest {

    private DeepseekServiceImpl deepseekService;
    
    @BeforeEach
    public void setup() {
        // 创建一个手动配置的DeepseekServiceImpl实例
        deepseekService = new DeepseekServiceImpl();
        
        // 使用反射设置必要的字段
        try {
            java.lang.reflect.Field apiUrlField = DeepseekServiceImpl.class.getDeclaredField("apiUrl");
            apiUrlField.setAccessible(true);
            apiUrlField.set(deepseekService, "https://dashscope.aliyuncs.com/compatible-mode/v1");
            
            java.lang.reflect.Field apiKeyField = DeepseekServiceImpl.class.getDeclaredField("apiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(deepseekService, "sk-90c41fa06627493a878b6afb10470472");
            
            java.lang.reflect.Field modelField = DeepseekServiceImpl.class.getDeclaredField("model");
            modelField.setAccessible(true);
            modelField.set(deepseekService, "qwen-turbo");
            
            java.lang.reflect.Field providerField = DeepseekServiceImpl.class.getDeclaredField("provider");
            providerField.setAccessible(true);
            providerField.set(deepseekService, "qwen");
            
            java.lang.reflect.Field temperatureField = DeepseekServiceImpl.class.getDeclaredField("temperature");
            temperatureField.setAccessible(true);
            temperatureField.set(deepseekService, 0.3);
            
            java.lang.reflect.Field endpointField = DeepseekServiceImpl.class.getDeclaredField("endpoint");
            endpointField.setAccessible(true);
            endpointField.set(deepseekService, "/chat/completions");
            
            // 模拟Environment依赖
            Environment envMock = mock(Environment.class);
            when(envMock.getProperty(eq("judge.ai-workspace"), anyString())).thenReturn("E:/temp/oj-project/ai-judge");
            
            java.lang.reflect.Field envField = DeepseekServiceImpl.class.getDeclaredField("environment");
            envField.setAccessible(true);
            envField.set(deepseekService, envMock);
            
        } catch (Exception e) {
            log.error("设置测试字段失败", e);
            fail("无法设置测试环境: " + e.getMessage());
        }
    }
    
    @Test
    public void testApiUrl() {
        // 使用反射获取私有方法
        try {
            java.lang.reflect.Method buildApiUrlMethod = DeepseekServiceImpl.class.getDeclaredMethod("buildApiUrl");
            buildApiUrlMethod.setAccessible(true);
            
            String apiUrl = (String) buildApiUrlMethod.invoke(deepseekService);
            assertEquals("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", apiUrl, "API URL应该正确构建");
            log.info("API URL测试通过: {}", apiUrl);
        } catch (Exception e) {
            log.error("测试API URL失败", e);
            fail("测试API URL失败: " + e.getMessage());
        }
    }
    
    @Test
    public void testExtractJson() {
        try {
            java.lang.reflect.Method extractJsonMethod = DeepseekServiceImpl.class.getDeclaredMethod("extractJson", String.class);
            extractJsonMethod.setAccessible(true);
            
            String testResponse = "这是一些文本 {\"key\": \"value\"} 后面跟着更多文本";
            String json = (String) extractJsonMethod.invoke(deepseekService, testResponse);
            assertEquals("{\"key\": \"value\"}", json, "应该正确提取JSON");
            log.info("JSON提取测试通过: {}", json);
        } catch (Exception e) {
            log.error("测试JSON提取失败", e);
            fail("测试JSON提取失败: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetFileExtension() {
        try {
            java.lang.reflect.Method getFileExtensionMethod = DeepseekServiceImpl.class.getDeclaredMethod("getFileExtension", String.class);
            getFileExtensionMethod.setAccessible(true);
            
            assertEquals("java", getFileExtensionMethod.invoke(deepseekService, "java"), "Java扩展名应该正确");
            assertEquals("py", getFileExtensionMethod.invoke(deepseekService, "python"), "Python扩展名应该正确");
            assertEquals("cpp", getFileExtensionMethod.invoke(deepseekService, "c++"), "C++扩展名应该正确");
            assertEquals("js", getFileExtensionMethod.invoke(deepseekService, "javascript"), "JavaScript扩展名应该正确");
            
            log.info("获取文件扩展名测试通过");
        } catch (Exception e) {
            log.error("测试获取文件扩展名失败", e);
            fail("测试获取文件扩展名失败: " + e.getMessage());
        }
    }
} 