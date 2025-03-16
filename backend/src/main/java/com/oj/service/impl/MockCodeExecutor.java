package com.oj.service.impl;

import com.oj.constant.JudgeConstant;
import com.oj.model.dto.CompileResult;
import com.oj.model.dto.ExecutionResult;
import com.oj.service.CodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 模拟代码执行器（用于Docker不可用的环境）
 */
@Service
@Slf4j
@Primary
public class MockCodeExecutor implements CodeExecutor {

    @Override
    public CompileResult compile(String code, String language) {
        log.info("模拟编译代码，语言: {}", language);
        
        // 模拟代码编译过程
        // 在真实项目中，应该根据语言进行实际的编译
        return CompileResult.builder()
                .success(true)
                .compiledFilePath("mock://" + language + "/solution")
                .build();
    }

    @Override
    public ExecutionResult execute(String compiledCode, String input, Long timeLimit, Long memoryLimit) {
        log.info("模拟执行代码，输入: {}, 时间限制: {}ms, 内存限制: {}KB", 
                input, timeLimit, memoryLimit);
        
        // 模拟代码执行过程
        String output;
        
        // 简单的模拟测试：将输入原样返回作为输出
        if (input != null && !input.isEmpty()) {
            output = "模拟输出: " + input;
        } else {
            output = "模拟输出: 没有输入";
        }
        
        return ExecutionResult.builder()
                .success(true)
                .status(JudgeConstant.STATUS_ACCEPTED)
                .output(output)
                .executionTime(50L) // 模拟执行时间50ms
                .memoryUsage(10240L) // 模拟内存使用10MB
                .build();
    }
} 