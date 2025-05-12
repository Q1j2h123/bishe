package com.oj.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

// 选择判断题提交请求
@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceJudgeSubmissionRequest extends SubmissionRequest {
    private String answer; // 单选："A"，多选："A,B,C"，判断："true"/"false"
}