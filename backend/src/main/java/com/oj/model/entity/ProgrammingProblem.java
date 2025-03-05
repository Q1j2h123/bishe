package com.oj.model.entity;

import lombok.Data;

@Data
public class ProgrammingProblem {
    private Long id;
    private String testCases;     // JSON格式的测试用例
    private String sampleInput;   // 示例输入
    private String sampleOutput;  // 示例输出
    private Integer timeLimit;    // 时间限制(ms)
    private Integer memoryLimit;  // 内存限制(MB)
    private String templateCode;  // 模板代码
} 