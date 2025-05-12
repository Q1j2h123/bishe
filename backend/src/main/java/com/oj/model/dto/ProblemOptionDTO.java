package com.oj.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ProblemOptionDTO implements Serializable {
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