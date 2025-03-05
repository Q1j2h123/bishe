package com.oj.model.entity;

import lombok.Data;

@Data
public class ChoiceProblem {
    private Long id;
    private String options;  // JSON格式：[{"key": "A", "content": "选项内容"}]
    private String answer;   // 单选：A/B/C/D，多选：逗号分隔
    private String analysis;
} 