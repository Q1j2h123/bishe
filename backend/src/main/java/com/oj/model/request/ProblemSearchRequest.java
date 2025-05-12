package com.oj.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 题目高级搜索请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "题目高级搜索请求")
public class ProblemSearchRequest extends PageRequest implements Serializable {

    /**
     * 关键词（多关键词逗号分隔）
     */
    @ApiModelProperty(value = "关键词（多关键词逗号分隔）", example = "Java,数组,递归")
    private String keywords;

    /**
     * 多种题目类型
     */
    @ApiModelProperty(value = "题目类型列表", example = "[\"CHOICE\",\"PROGRAM\"]")
    private List<String> typeList;

    /**
     * 多种难度
     */
    @ApiModelProperty(value = "难度列表", example = "[\"EASY\",\"MEDIUM\"]")
    private List<String> difficultyList;

    /**
     * 多种岗位
     */
    @ApiModelProperty(value = "岗位列表", example = "[\"前端\",\"后端\"]")
    private List<String> jobTypeList;

    /**
     * 多种状态
     */
    @ApiModelProperty(value = "状态列表", example = "[\"UNSOLVED\",\"ATTEMPTED\"]")
    private List<String> statusList;

    /**
     * 标签列表
     */
    @ApiModelProperty(value = "标签列表", example = "[\"算法\",\"数据结构\"]")
    private List<String> tags;

    /**
     * 创建者ID列表
     */
    @ApiModelProperty(value = "创建者ID列表", example = "[1,2,3]")
    private List<Long> creatorIds;

    /**
     * 是否只看自己创建的
     */
    @ApiModelProperty(value = "是否只看自己创建的", example = "false")
    private Boolean onlyMine = false;

    /**
     * 最小提交数
     */
    @ApiModelProperty(value = "最小提交数", example = "10")
    private Integer minSubmissionCount;

    /**
     * 最大提交数
     */
    @ApiModelProperty(value = "最大提交数", example = "1000")
    private Integer maxSubmissionCount;

    /**
     * 最小通过率（百分比）
     */
    @ApiModelProperty(value = "最小通过率（百分比）", example = "50")
    private Integer minAcceptRate;

    /**
     * 最大通过率（百分比）
     */
    @ApiModelProperty(value = "最大通过率（百分比）", example = "100")
    private Integer maxAcceptRate;

    private static final long serialVersionUID = 1L;
} 