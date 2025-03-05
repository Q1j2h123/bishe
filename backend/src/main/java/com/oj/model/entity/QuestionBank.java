package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oj.model.enums.JobTypeEnum;
import com.oj.model.enums.ProblemTypeEnum;
import com.oj.model.enums.QuestionBankCategoryEnum;
import com.oj.model.enums.QuestionBankLevelEnum;
import com.oj.model.enums.QuestionBankPermissionEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题库
 */
@TableName(value = "question_bank")
@Data
public class QuestionBank implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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

    /**
     * 创建者 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
} 