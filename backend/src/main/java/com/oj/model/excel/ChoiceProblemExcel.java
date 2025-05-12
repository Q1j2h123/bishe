package com.oj.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 选择题Excel导入模型
 */
@Data
public class ChoiceProblemExcel {
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
     * 选项A
     */
    @ExcelProperty("选项A")
    private String optionA;

    /**
     * 选项B
     */
    @ExcelProperty("选项B")
    private String optionB;

    /**
     * 选项C
     */
    @ExcelProperty("选项C")
    private String optionC;

    /**
     * 选项D
     */
    @ExcelProperty("选项D")
    private String optionD;

    /**
     * 答案（A, B, C, D）
     */
    @ExcelProperty("正确答案")
    private String answer;

    /**
     * 解析
     */
    @ExcelProperty("解析")
    private String analysis;

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
} 