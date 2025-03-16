package com.oj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 编译结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompileResult {
    /**
     * 是否编译成功
     */
    private Boolean success;
    
    /**
     * 编译错误信息
     */
    private String errorMessage;
    
    /**
     * 编译后的代码或二进制文件路径
     */
    private String compiledFilePath;
} 