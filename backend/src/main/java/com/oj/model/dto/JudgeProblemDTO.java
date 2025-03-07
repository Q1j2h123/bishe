package com.oj.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class JudgeProblemDTO extends ProblemDTO {
    /**
     * 正确答案
     */
    private Boolean answer;

    /**
     * 解析
     */
    private String analysis;

    private static final long serialVersionUID = 1L;
} 