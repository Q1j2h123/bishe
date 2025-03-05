package com.oj.model.dto;

import com.oj.model.enums.JobTypeEnum;
import com.oj.model.enums.ProblemTypeEnum;
import com.oj.model.enums.QuestionBankCategoryEnum;
import com.oj.model.enums.QuestionBankLevelEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 题库查询请求
 */
@Data
public class QuestionBankQueryRequest implements Serializable {

    /**
     * 题库名称
     */
    private String name;

    /**
     * 岗位类型
     */
    private JobTypeEnum jobType;

    /**
     * 难度等级
     */
    private QuestionBankLevelEnum level;

    /**
     * 分类
     */
    private QuestionBankCategoryEnum category;

    /**
     * 题目类型
     */
    private ProblemTypeEnum type;

    /**
     * 标签
     */
    private String tags;

    /**
     * 当前页码
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "ascend";

    private static final long serialVersionUID = 1L;
} 