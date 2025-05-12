package com.oj.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

// 编程题提交VO
@Data
@EqualsAndHashCode(callSuper = true)
public class ProgramSubmissionListVO extends SubmissionListVO {
    private String language;
    private Integer executeTime;
    private Integer memoryUsage;
    
}