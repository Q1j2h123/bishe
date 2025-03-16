package com.oj.service;

import com.oj.model.dto.CompileResult;
import com.oj.model.dto.ExecutionResult;

/**
 * 代码执行器
 */
public interface CodeExecutor {
    /**
     * 编译代码
     * @param code 源代码
     * @param language 编程语言
     * @return 编译结果
     */
    CompileResult compile(String code, String language);
    
    /**
     * 运行代码
     * @param compiledCode 编译后的代码或文件路径
     * @param input 输入数据
     * @param timeLimit 时间限制(ms)
     * @param memoryLimit 内存限制(KB)
     * @return 执行结果
     */
    ExecutionResult execute(String compiledCode, String input, Long timeLimit, Long memoryLimit);
} 