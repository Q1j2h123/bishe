package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("choice_problem")
public class ChoiceProblem {
    /**
     * 关联problem表的id
     */
    private Long id;

    /**
     * 选项，JSON格式：[{"key": "A", "content": "选项内容"}]
     */
    private String options;

    /**
     * 正确答案，单选为A/B/C/D，多选为逗号分隔
     */
    private String answer;

    /**
     * 解析
     */
    private String analysis;
} 