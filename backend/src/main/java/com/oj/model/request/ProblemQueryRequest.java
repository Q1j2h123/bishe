package com.oj.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 题目查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "题目查询请求")
public class ProblemQueryRequest extends PageRequest implements Serializable {

    @ApiModelProperty(value = "题目标题/内容搜索关键词")
    private String searchText;

    @ApiModelProperty(value = "题目ID")
    private Long id;

    @ApiModelProperty(value = "题目类型（CHOICE-选择题, JUDGE-判断题, PROGRAM-编程题）")
    private String type;

    @ApiModelProperty(value = "岗位类型")
    private String jobType;

    @ApiModelProperty(value = "题目难度（EASY-简单, MEDIUM-中等, HARD-困难）")
    private String difficulty;

    @ApiModelProperty(value = "单个标签（兼容提交列表的标签查询）")
    private String tag;

    @ApiModelProperty(value = "标签列表")
    private List<String> tags;

    @ApiModelProperty(value = "创建用户ID")
    private Long userId;

    @ApiModelProperty(value = "题目状态（UNSOLVED-未解决，ATTEMPTED-尝试过，SOLVED-已解决）")
    private String status;

    private static final long serialVersionUID = 1L;
} 