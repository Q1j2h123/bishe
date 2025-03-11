package com.oj.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class JudgeProblemUpdateRequest extends ProblemUpdateRequest {
    /**
     * 正确答案
     */
    @NotNull(message = "答案不能为空")
    private Boolean answer;

    /**
     * 解析
     */
    private String analysis;
} 