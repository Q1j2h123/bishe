package com.oj.model.dto;

import com.oj.model.enums.JobTypeEnum;
import com.oj.model.enums.ProblemTypeEnum;
import com.oj.model.enums.QuestionBankCategoryEnum;
import com.oj.model.enums.QuestionBankLevelEnum;
import com.oj.model.enums.QuestionBankPermissionEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 题库添加请求
 */
@Data
public class QuestionBankAddRequest implements Serializable {

    /**
     * 题库名称
     */
    private String name;

    /**
     * 题库描述
     */
    private String description;

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
     * 标签，多个标签用逗号分隔
     */
    private String tags;

    /**
     * 权限
     */
    private QuestionBankPermissionEnum permission;

    private static final long serialVersionUID = 1L;
} 