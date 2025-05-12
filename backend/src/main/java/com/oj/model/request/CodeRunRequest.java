package com.oj.model.request;

import lombok.Data;

/**
 * 运行代码请求
 */
@Data
public class CodeRunRequest {
    /**
     * 题目ID
     */
    private Long problemId;
    
    /**
     * 编程语言
     */
    private String language;
    
    /**
     * 代码内容
     */
    private String code;
} 