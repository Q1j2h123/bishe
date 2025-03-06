package com.oj.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ProgrammingProblemDTO implements Serializable {
    /**
     * 函数名称
     */
    private String functionName;

    /**
     * 参数类型列表
     */
    private List<String> paramTypes;

    /**
     * 返回值类型
     */
    private String returnType;

    /**
     * 测试用例列表
     */
    private List<TestCaseDTO> testCases;

    /**
     * 代码模板（key为语言类型，value为模板代码）
     */
    private Map<String, String> templates;

    /**
     * 时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 内存限制(MB)
     */
    private Integer memoryLimit;

    private static final long serialVersionUID = 1L;
}

@Data
class TestCaseDTO implements Serializable {
    /**
     * 输入数据
     */
    private String input;

    /**
     * 期望输出
     */
    private String output;

    /**
     * 是否为示例
     */
    private Boolean isExample;

    private static final long serialVersionUID = 1L;
} 