package com.oj.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProblemUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 题目类型
     */
    private String type;

    /**
     * 岗位类型
     */
    private String jobType;

    /**
     * 题目标签
     */
    private String tags;

    /**
     * 选择题选项
     */
    private String options;

    /**
     * 答案
     */
    private String answer;

    /**
     * 题目解析
     */
    private String analysis;

    /**
     * 难度
     */
    private String difficulty;
} 