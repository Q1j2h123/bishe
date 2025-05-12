package com.oj.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

// 选择/判断题提交VO
@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceJudgeSubmissionListVO extends SubmissionListVO {
    private String answer; // 用户的答案
}