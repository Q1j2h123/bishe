package com.oj.model.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 题目更新请求
 */
@Data
public class ProblemUpdateRequest {
    /**
     * 题目id
     */
    @NotNull(message = "题目id不能为空")
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 题目类型（选择题-CHOICE, 判断题-JUDGE, 编程题-program）
     */
    private String type;

    /**
     * 岗位类型
     */
    private String jobType;

    /**
     * 难度（EASY, MEDIUM, HARD）
     */
    private String difficulty;

    /**
     * 标签列表
     */
    private List<String> tags;
} 