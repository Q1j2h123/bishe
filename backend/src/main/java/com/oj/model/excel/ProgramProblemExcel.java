package com.oj.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 编程题Excel导入模型
 */
@Data
public class ProgramProblemExcel {
    /**
     * 标题
     */
    @ExcelProperty("题目标题")
    private String title;

    /**
     * 内容
     */
    @ExcelProperty("题目内容")
    private String content;

    /**
     * 函数名称
     */
    @ExcelProperty("函数名称")
    private String functionName;

    /**
     * 参数类型列表，逗号分隔
     */
    @ExcelProperty("参数类型")
    private String paramTypes;

    /**
     * 返回值类型
     */
    @ExcelProperty("返回值类型")
    private String returnType;

    /**
     * 测试用例输入，使用###分隔多个用例
     */
    @ExcelProperty("测试用例输入")
    private String testCaseInputs;

    /**
     * 测试用例输出，使用###分隔多个用例
     */
    @ExcelProperty("测试用例输出")
    private String testCaseOutputs;

    /**
     * Java代码模板
     */
    @ExcelProperty("Java代码模板")
    private String javaTemplate;

    /**
     * Python代码模板
     */
    @ExcelProperty("Python代码模板")
    private String pythonTemplate;

    /**
     * C++代码模板
     */
    @ExcelProperty("C++代码模板")
    private String cppTemplate;
    @ExcelProperty("C代码模板")
    private String cTemplate;

    /**
     * Java语言标准答案
     */
    @ExcelProperty("Java标准答案")
    private String javaSolution;

    /**
     * Python语言标准答案
     */
    @ExcelProperty("Python标准答案")
    private String pythonSolution;

    /**
     * C++语言标准答案
     */
    @ExcelProperty("C++标准答案")
    private String cppSolution;

    /**
     * C语言标准答案
     */
    @ExcelProperty("C标准答案")
    private String cSolution;

    /**
     * 时间限制（毫秒）
     */
    @ExcelProperty("时间限制(ms)")
    private Integer timeLimit;

    /**
     * 内存限制（MB）
     */
    @ExcelProperty("内存限制(MB)")
    private Integer memoryLimit;

    /**
     * 难度（EASY, MEDIUM, HARD）
     */
    @ExcelProperty("难度")
    private String difficulty;

    /**
     * 岗位类型
     */
    @ExcelProperty("岗位类型")
    private String jobType;

    /**
     * 标签，逗号分隔
     */
    @ExcelProperty("标签")
    private String tags;

    public String getJavaSolution() {
        return javaSolution;
    }

    public void setJavaSolution(String javaSolution) {
        this.javaSolution = javaSolution;
    }

    public String getPythonSolution() {
        return pythonSolution;
    }

    public void setPythonSolution(String pythonSolution) {
        this.pythonSolution = pythonSolution;
    }

    public String getCppSolution() {
        return cppSolution;
    }

    public void setCppSolution(String cppSolution) {
        this.cppSolution = cppSolution;
    }

    public String getCSolution() {
        return cSolution;
    }

    public void setCSolution(String cSolution) {
        this.cSolution = cSolution;
    }
} 