package com.oj.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class programProblemDTO extends ProblemDTO {
    /**
     * 函数名
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
     * 代码模板
     */
    private Map<String, String> templates;

    /**
     * 标准答案
     */
    private Map<String, String> standardSolution;

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

