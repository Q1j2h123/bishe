package com.oj.service.impl;

import com.oj.constant.JudgeConstant;
import com.oj.model.dto.CompileResult;
import com.oj.model.dto.ExecutionResult;
import com.oj.service.CodeExecutor;
import com.oj.service.DeepseekService;
import com.oj.service.OpenAIService;
import com.oj.utils.JudgeContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * AI代码执行器
 * 使用AI来编译和评判代码，无需Docker环境
 */
@Service
@Slf4j
public class AICodeExecutor implements CodeExecutor {

    @Resource
    private DeepseekService deepseekService;
    
    @Resource
    private OpenAIService openAIService;
    
    @Resource
    private Environment environment;
    
    @Value("${ai.primary-service:deepseek}")
    private String primaryService;
    
    @Value("${ai.fallback-enabled:true}")
    private boolean fallbackEnabled;
    
    @Override
    public CompileResult compile(String code, String language) {
        // 获取当前处理的提交ID
        Long submissionId = JudgeContextHolder.getSubmissionId();
        log.info("AI编译代码，语言: {}, 提交ID: {}", language, submissionId);
        
        boolean valid;
        try {
            // 根据配置选择使用哪个AI服务
            if ("openai".equalsIgnoreCase(primaryService)) {
                valid = openAIService.validateCodeSyntax(code, language);
            } else {
                valid = deepseekService.validateCodeSyntax(code, language);
            }
        } catch (Exception e) {
            log.warn("主要AI服务不可用: {}", e.getMessage());
            
            if (fallbackEnabled) {
                log.info("尝试使用备用AI服务");
                try {
                    // 如果主服务是OpenAI，则回退到Deepseek，反之亦然
                    if ("openai".equalsIgnoreCase(primaryService)) {
                        valid = deepseekService.validateCodeSyntax(code, language);
                    } else {
                        valid = openAIService.validateCodeSyntax(code, language);
                    }
                } catch (Exception fallbackEx) {
                    log.error("备用AI服务也不可用: {}", fallbackEx.getMessage());
                    // 两个服务都失败时，假设代码正确，让系统继续处理
                    valid = true;
                }
            } else {
                log.info("备用服务未启用，假设代码正确");
                valid = true;
            }
        }
        
        if (!valid) {
            String errorMessage;
            try {
                // 根据配置选择使用哪个AI服务
                if ("openai".equalsIgnoreCase(primaryService)) {
                    errorMessage = openAIService.getCompileErrorMessage(code, language);
                } else {
                    errorMessage = deepseekService.getCompileErrorMessage(code, language);
                }
            } catch (Exception e) {
                log.warn("主要AI服务不可用: {}", e.getMessage());
                
                if (fallbackEnabled) {
                    try {
                        // 尝试使用备用服务
                        if ("openai".equalsIgnoreCase(primaryService)) {
                            errorMessage = deepseekService.getCompileErrorMessage(code, language);
                        } else {
                            errorMessage = openAIService.getCompileErrorMessage(code, language);
                        }
                    } catch (Exception fallbackEx) {
                        log.error("备用AI服务也不可用: {}", fallbackEx.getMessage());
                        errorMessage = "无法获取具体错误信息: 所有AI服务都不可用";
                    }
                } else {
                    errorMessage = "无法获取具体错误信息: 备用服务未启用";
                }
            }
            
            return CompileResult.builder()
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();
        }
        
        // 创建特殊的编译结果，包含提交ID
        String compiledFilePath = "ai://" + language + "/solution_" + submissionId;
        
        return CompileResult.builder()
                .success(true)
                .compiledFilePath(compiledFilePath)
                .build();
    }

    @Override
    public ExecutionResult execute(String compiledCode, String input, Long timeLimit, Long memoryLimit) {
        // 获取当前处理的提交ID
        Long submissionId = JudgeContextHolder.getSubmissionId();
        log.info("AI执行代码评判，输入: {}, 时间限制: {}ms, 内存限制: {}KB, 提交ID: {}", 
                input, timeLimit, memoryLimit, submissionId);
        
        String language = null;
        
        // 从compiledCode路径中解析语言信息
        if (compiledCode.startsWith("ai://")) {
            String[] parts = compiledCode.split("://")[1].split("/");
            language = parts[0];
        }
        
        if (language == null) {
            return ExecutionResult.builder()
                    .success(false)
                    .status(JudgeConstant.STATUS_SYSTEM_ERROR)
                    .errorMessage("无法解析语言类型")
                    .build();
        }
        
        try {
            // 根据配置选择使用哪个AI服务
            if ("openai".equalsIgnoreCase(primaryService)) {
                return openAIService.evaluateCode(compiledCode, input, language);
            } else {
                return deepseekService.evaluateCode(compiledCode, input, language);
            }
        } catch (Exception e) {
            log.warn("主要AI服务执行失败: {}", e.getMessage());
            
            if (fallbackEnabled) {
                log.info("尝试使用备用AI服务");
                try {
                    // 尝试使用备用服务
                    if ("openai".equalsIgnoreCase(primaryService)) {
                        return deepseekService.evaluateCode(compiledCode, input, language);
                    } else {
                        return openAIService.evaluateCode(compiledCode, input, language);
                    }
                } catch (Exception fallbackEx) {
                    log.error("备用AI服务也执行失败: {}", fallbackEx.getMessage());
                    return ExecutionResult.builder()
                            .success(false)
                            .status(JudgeConstant.STATUS_SYSTEM_ERROR)
                            .errorMessage("所有AI服务都不可用: " + e.getMessage())
                            .build();
                }
            } else {
                log.warn("备用服务未启用，返回错误");
                return ExecutionResult.builder()
                        .success(false)
                        .status(JudgeConstant.STATUS_SYSTEM_ERROR)
                        .errorMessage("AI服务不可用且备用服务未启用: " + e.getMessage())
                        .build();
            }
        }
    }
} 