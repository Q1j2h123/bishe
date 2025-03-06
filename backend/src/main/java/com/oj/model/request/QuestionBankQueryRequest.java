package com.oj.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionBankQueryRequest extends PageRequest {
    /**
     * 题库名称
     */
    private String name;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 标签
     */
    private String tag;

    /**
     * 创建者id
     */
    private Long userId;

    /**
     * 权限
     */
    private String permission;
} 