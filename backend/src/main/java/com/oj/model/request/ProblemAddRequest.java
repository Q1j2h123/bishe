package com.oj.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

@Data
public class ProblemAddRequest {
    @NotBlank(message = "题目标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100")
    private String title;
    @NotBlank(message = "题目内容不能为空")
    private String content;
    @NotBlank(message = "题目类型不能为空")
    @Pattern(regexp = "CHOICE|JUDGE|PROGRAM", message = "题目类型不合法")
    private String type;
    @NotBlank(message = "岗位类型不能为空")
    private String jobType;
    private String tags;
    @NotBlank(message = "难度不能为空")
    @Pattern(regexp = "EASY|MEDIUM|HARD", message = "难度值不合法")
    private String difficulty;
    /**
     * 具体题目内容（选择题、判断题、编程题）
     */
    @NotNull(message = "题目详情不能为空")
    private Object problemDetail;
}