package com.oj.service;

import com.oj.model.dto.ExecutionResult;

/**
 * OpenAI服务接口
 * 用于与OpenAI API进行交互，实现代码评判功能
 */
public interface OpenAIService {
    
    /**
     * 验证代码语法
     * 
     * @param code 代码内容
     * @param language 编程语言
     * @return 语法是否正确
     */
    boolean validateCodeSyntax(String code, String language);
    
    /**
     * 获取编译错误信息
     * 
     * @param code 代码内容
     * @param language 编程语言
     * @return 错误信息
     */
    String getCompileErrorMessage(String code, String language);
    
    /**
     * 评估代码
     * 
     * @param compiledCode 编译后的代码路径
     * @param input 测试用例输入
     * @param language 编程语言
     * @return 执行结果
     */
    ExecutionResult evaluateCode(String compiledCode, String input, String language);
} 