package com.oj.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionBankUpdateRequest extends QuestionBankAddRequest {
    @NotNull(message = "题库id不能为空")
    private Long id;
} 