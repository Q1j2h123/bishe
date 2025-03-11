package com.oj.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 题目添加请求
 */
@Data
public class ProblemAddRequest {
    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 题目类型（选择题-CHOICE, 判断题-JUDGE, 编程题-program）
     */
    @NotBlank(message = "题目类型不能为空")
    private String type;

    /**
     * 岗位类型
     */
    private String jobType;

    /**
     * 难度（EASY, MEDIUM, HARD）
     */
    @NotBlank(message = "难度不能为空")
    private String difficulty;

    /**
     * 标签列表
     */
    private List<String> tags;

} 