package com.oj.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProblemQueryRequest extends PageRequest {
    /**
     * 标题关键词
     */
    private String title;

    /**
     * 岗位方向
     */
    private String jobType;

    /**
     * 状态（UNSOLVED-未解决，ATTEMPTED-尝试过，SOLVED-已解决）
     */
    private String status;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 标签（多个标签用逗号分隔）
     */
    private String tags;

    /**
     * 题目类型
     */
    private String type;

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