package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("judge_problem")
public class JudgeProblem {
    /**
     * 关联problem表的id
     */
    private Long id;

    /**
     * 正确答案：true(正确)/false(错误)
     */
    private Boolean answer;

    /**
     * 解析
     */
    private String analysis;
} 