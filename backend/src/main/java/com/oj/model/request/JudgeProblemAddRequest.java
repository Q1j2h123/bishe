package com.oj.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class JudgeProblemAddRequest extends ProblemAddRequest {
    @NotNull(message = "答案不能为空")
    private Boolean answer;

    private String analysis;
} 