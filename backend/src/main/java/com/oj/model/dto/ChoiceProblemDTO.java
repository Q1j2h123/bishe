package com.oj.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ChoiceProblemDTO implements Serializable {
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

@Data
class ProblemOptionDTO implements Serializable {
    /**
     * 选项键（A、B、C、D）
     */
    private String key;

    /**
     * 选项内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
} 