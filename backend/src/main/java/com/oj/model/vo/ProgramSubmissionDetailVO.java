package com.oj.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProgramSubmissionDetailVO extends SubmissionDetailVO {
    // 继承自SubmissionDetailVO的共有字段
    private String userName;
    private String problemContent;
    private List<String> problemTags;
    private String jobType;
    private String difficulty;
    
    // 编程题特有字段
    private String code;              // 提交的代码
    private String language;          // 编程语言
    private String errorMessage;      // 错误信息
    private String testcaseResults;   // 测试用例结果JSON
    private Integer passedTestCases;  // 通过测试用例数
    private Integer totalTestCases;   // 总测试用例数
    private Integer executeTime;      // 执行时间(ms)
    private Integer memoryUsage;      // 内存使用(KB)
    
    // 题目要求相关
    private String functionName;      // 函数名称
    private List<String> paramTypes;  // 参数类型列表
    private String returnType;        // 返回值类型
    private Integer timeLimit;        // 时间限制(ms)
    private Integer memoryLimit;      // 内存限制(MB)
    
    // 标准答案 - 只对管理员和题目创建者可见
    private Map<String, String> standardSolution;
}