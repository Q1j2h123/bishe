package com.oj.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProblemUpdateRequest extends ProblemAddRequest {
    /**
     * id
     */
    @NotNull(message = "题目id不能为空")
    private Long id;
} 