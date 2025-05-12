package com.oj.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

// 基础提交详情VO
@Data
@EqualsAndHashCode(callSuper = true)
public class SubmissionDetailVO extends SubmissionListVO {
    // 提交者信息
    private String userName;
    
    // 题目详细信息
    private String problemContent;  // 题目内容
    private List<String> problemTags; // 题目标签
    private String jobType;         // 岗位类型
    private String difficulty;      // 题目难度
    
    // 操作相关
    private Boolean canViewAnalysis; // 是否可以查看解析（根据业务规则决定）
}