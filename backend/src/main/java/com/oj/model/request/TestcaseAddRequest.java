package com.oj.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 添加测试用例请求
 */
@Data
@ApiModel(description = "添加测试用例请求")
public class TestcaseAddRequest {
    
    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    @ApiModelProperty(value = "题目ID", required = true, example = "1")
    private Long problemId;
    
    /**
     * 输入数据
     */
    @NotBlank(message = "输入数据不能为空")
    @ApiModelProperty(value = "输入数据", required = true, example = "1 2")
    private String input;
    
    /**
     * 期望输出
     */
    @NotBlank(message = "期望输出不能为空")
    @ApiModelProperty(value = "期望输出", required = true, example = "3")
    private String expectedOutput;
    
    /**
     * 是否为示例测试用例
     */
    @NotNull(message = "是否为示例不能为空")
    @ApiModelProperty(value = "是否为示例测试用例", required = true, example = "true")
    private Boolean isExample;
} 