package com.oj.model.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BankProblemRequest {
    @NotNull(message = "题库id不能为空")
    private Long bankId;

    @NotNull(message = "题目id不能为空")
    private Long problemId;
} 