package com.oj.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 错题本查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorProblemQueryRequest extends PageRequest implements Serializable {

    /**
     * 题目类型
     */
    private String type;

    /**
     * 题目难度
     */
    private String difficulty;

    /**
     * 关键词
     */
    private String keyword;

    private static final long serialVersionUID = 1L;
} 