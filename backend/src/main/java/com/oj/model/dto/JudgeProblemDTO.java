package com.oj.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class JudgeProblemDTO implements Serializable {
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