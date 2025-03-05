package com.oj.model.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 题目Excel导入导出DTO
 */
@Data
public class ProblemExcelDTO {
    
    @ExcelProperty("题目名称")
    private String title;
    
    @ExcelProperty("题目内容")
    private String content;
    
    @ExcelProperty("题目类型")
    private String type;  // CHOICE, JUDGE, PROGRAM
    
    @ExcelProperty("职业类型")
    private String jobType;
    
    @ExcelProperty("标签")
    private String tags;
    
    @ExcelProperty("难度")
    private String difficulty;

    // 选择题专用字段
    @ExcelProperty("选项")
    private String options;  // JSON格式：[{"key": "A", "content": "选项内容"}]

    @ExcelProperty("答案")
    private String answer;   // 选择题：A/B/C/D，判断题：true/false，编程题：模板代码

    @ExcelProperty("解析")
    private String analysis;

    // 编程题专用字段
    @ExcelProperty("测试用例")
    private String testCases;  // JSON格式

    @ExcelProperty("示例输入")
    private String sampleInput;

    @ExcelProperty("示例输出")
    private String sampleOutput;

    @ExcelProperty("时间限制(ms)")
    private Integer timeLimit;

    @ExcelProperty("内存限制(MB)")
    private Integer memoryLimit;
} 