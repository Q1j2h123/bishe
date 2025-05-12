package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 编程题提交详情实体类
@Data
@TableName("program_submission")
public class ProgramSubmission {
    @TableId
    private Long submissionId;
    private String language;
    private String code;
    private Integer executeTime;
    private Integer memoryUsage;
    private String testcaseResults;
    private String errorMessage;
}