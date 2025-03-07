package com.oj.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceProblemDTO extends ProblemDTO {
    /**
     * 选项列表
     */
    private List<ProblemOptionDTO> options;

    /**
     * 正确答案
     */
    private String answer;

    /**
     * 解析
     */
    private String analysis;

    private static final long serialVersionUID = 1L;
}

