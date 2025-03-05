package com.oj.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 提交答案请求
 */
@Data
@ApiModel("提交答案请求")
public class SubmissionRequest {
    
    @ApiModelProperty("题目ID")
    private Long problemId;

    @ApiModelProperty("题目类型：CHOICE/JUDGE/PROGRAM")
    private String type;

    @ApiModelProperty("答案内容")
    private String answer;

    @ApiModelProperty("编程语言（编程题专用）")
    private String language;
} 