package com.oj.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 判断题Excel导入模型
 */
@Data
public class JudgeProblemExcel {
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
     * 答案（true/false 或 是/否）
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