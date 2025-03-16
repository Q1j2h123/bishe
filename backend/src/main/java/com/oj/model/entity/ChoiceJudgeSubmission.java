package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 选择判断题提交详情实体类
@Data
@TableName("choice_judge_submission")
public class ChoiceJudgeSubmission {
    @TableId
    private Long submissionId;
    private String answer;
}