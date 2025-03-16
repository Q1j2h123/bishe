package com.oj.model.vo;

import com.oj.model.dto.ProblemOptionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceJudgeSubmissionDetailVO extends SubmissionDetailVO {
    // 继承自SubmissionDetailVO的共有字段
    private String userName;
    private String problemContent;
    private List<String> problemTags;
    private String jobType;
    private String difficulty;
    private Boolean canViewAnalysis;
    
    // 用户提交的答案
    private String answer;
    
    // 选择题特有字段
    private List<ProblemOptionDTO> options; // 选择题选项列表
    private String correctAnswer;     // 正确答案
    private String analysis;          // 题目解析（仅当canViewAnalysis为true时返回）
}