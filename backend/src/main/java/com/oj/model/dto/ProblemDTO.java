package com.oj.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ProblemDTO implements Serializable {
    /**
     * 题目id
     */
    private Long id;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 题目类型（CHOICE-选择题, JUDGE-判断题, PROGRAM-编程题）
     */
    private String type;

    /**
     * 岗位类型
     */
    private String jobType;

    /**
     * 题目难度（EASY-简单, MEDIUM-中等, HARD-困难）
     */
    private String difficulty;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 通过率
     */
    private String acceptRate;

    /**
     * 提交次数
     */
    private Integer submissionCount;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 题目详细信息（根据type返回不同的详细信息）
     */
    private String problemDetail;

    /**
     * 创建者名称
     */
    private String creatorName;

    /**
     * 状态（UNSOLVED-未解决，ATTEMPTED-尝试过，SOLVED-已解决）
     */
    private String status;

    private static final long serialVersionUID = 1L;
} 