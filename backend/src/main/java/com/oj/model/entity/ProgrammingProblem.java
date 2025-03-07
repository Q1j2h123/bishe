package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("programming_problem")
public class ProgrammingProblem {
    /**
     * 关联problem表的id
     */
    private Long id;

    /**
     * 函数名称
     */
    private String functionName;

    /**
     * 参数类型，JSON格式，如：["int[]", "int"]
     */
    private String paramTypes;

    /**
     * 返回值类型
     */
    private String returnType;

    /**
     * 测试用例，包含示例和隐藏用例，JSON格式
     */
    private String testCases;

    /**
     * 各语言代码模板，JSON格式
     */
    private String templates;

    /**
     * 各语言标准答案，JSON格式
     */
    private String standardSolution;

    /**
     * 时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 内存限制(MB)
     */
    private Integer memoryLimit;
} 