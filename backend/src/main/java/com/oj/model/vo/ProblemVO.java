package com.oj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.oj.model.entity.Problem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Data
@ApiModel(description = "题目视图对象")
public class ProblemVO implements Serializable {
    
    @ApiModelProperty(value = "题目ID")
    private Long id;

    @ApiModelProperty(value = "题目标题")
    private String title;

    @ApiModelProperty(value = "题目内容")
    private String content;

    @ApiModelProperty(value = "题目类型（CHOICE-选择题, JUDGE-判断题, PROGRAM-编程题）")
    private String type;

    @ApiModelProperty(value = "岗位类型")
    private String jobType;

    @ApiModelProperty(value = "题目难度（EASY-简单, MEDIUM-中等, HARD-困难）")
    private String difficulty;

    @ApiModelProperty(value = "标签列表")
    private List<String> tags;

    @ApiModelProperty(value = "通过率")
    private String acceptRate;

    @ApiModelProperty(value = "提交次数")
    private Integer submissionCount;

    @ApiModelProperty(value = "创建用户ID")
    private Long userId;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    /**
     * 包装类转对象
     */
    public static Problem voToObj(ProblemVO problemVO) {
        if (problemVO == null) {
            return null;
        }
        Problem problem = new Problem();
        problem.setId(problemVO.getId());
        problem.setTitle(problemVO.getTitle());
        problem.setContent(problemVO.getContent());
        problem.setType(problemVO.getType());
        problem.setJobType(problemVO.getJobType());
        problem.setDifficulty(problemVO.getDifficulty());
        problem.setAcceptRate(problemVO.getAcceptRate());
        problem.setSubmissionCount(problemVO.getSubmissionCount());
        problem.setUserId(problemVO.getUserId());
        problem.setCreateTime(problemVO.getCreateTime() != null ? 
            LocalDateTime.ofInstant(problemVO.getCreateTime().toInstant(), ZoneId.systemDefault()) : null);
        problem.setUpdateTime(problemVO.getUpdateTime() != null ? 
            LocalDateTime.ofInstant(problemVO.getUpdateTime().toInstant(), ZoneId.systemDefault()) : null);
        return problem;
    }

    /**
     * 对象转包装类
     */
    public static ProblemVO objToVo(Problem problem) {
        if (problem == null) {
            return null;
        }
        ProblemVO problemVO = new ProblemVO();
        problemVO.setId(problem.getId());
        problemVO.setTitle(problem.getTitle());
        problemVO.setContent(problem.getContent());
        problemVO.setType(problem.getType());
        problemVO.setJobType(problem.getJobType());
        problemVO.setDifficulty(problem.getDifficulty());
        problemVO.setAcceptRate(problem.getAcceptRate());
        problemVO.setSubmissionCount(problem.getSubmissionCount());
        problemVO.setUserId(problem.getUserId());
        problemVO.setCreateTime(problem.getCreateTime() != null ? 
            Date.from(problem.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()) : null);
        problemVO.setUpdateTime(problem.getUpdateTime() != null ? 
            Date.from(problem.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant()) : null);
        return problemVO;
    }
} 