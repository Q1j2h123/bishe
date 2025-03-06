package com.oj.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProblemQueryRequest extends PageRequest {
    /**
     * 标题
     */
    private String title;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 标签
     */
    private String tag;

    /**
     * 题目类型
     */
    private String type;

    /**
     * 岗位类型
     */
    private String jobType;

    /**
     * 创建者id
     */
    private Long userId;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder;
} 