package com.oj.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProblemQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页号
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 搜索关键词
     */
    private String searchText;

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
     * 难度
     */
    private String difficulty;

    /**
     * 排序字段
     */
    private String sortField = "createTime";

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "desc";
} 