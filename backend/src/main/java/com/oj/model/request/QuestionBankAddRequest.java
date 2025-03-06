package com.oj.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class QuestionBankAddRequest {
    @NotBlank(message = "题库名称不能为空")
    private String name;
    private String description;
    @NotBlank(message = "难度不能为空")
    private String difficulty;
    private String tags;
    private String permission = "PRIVATE";
    private Integer isHot = 0;
    private Integer isRecommended = 0;
}