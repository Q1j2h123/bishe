package com.oj.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class QuestionBankDTO implements Serializable {
    /**
     * 题库id
     */
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
     * 难度（EASY-简单, MEDIUM-中等, HARD-困难）
     */
    private String difficulty;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 权限（PUBLIC-公开, PRIVATE-私有, SHARED-共享）
     */
    private String permission;

    /**
     * 是否热门（0-否, 1-是）
     */
    private Integer isHot;

    /**
     * 是否推荐（0-否, 1-是）
     */
    private Integer isRecommended;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 题目数量
     */
    private Integer problemCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
} 