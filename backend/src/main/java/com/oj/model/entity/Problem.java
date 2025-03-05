package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("problem")
public class Problem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("type")
    private String type;  // 题目类型：CHOICE(选择题)、JUDGE(判断题)、PROGRAM(编程题)

    @TableField("jobType")
    private String jobType;  // 岗位类型：FRONTEND(前端)、BACKEND(后端)、ALGORITHM(算法)等

    @TableField("tags")
    private String tags;  // 题目标签，多个标签用逗号分隔

    @TableField("options")
    private String options;  // 选择题选项，JSON格式存储

    @TableField("answer")
    private String answer;  // 答案

    @TableField("analysis")
    private String analysis;  // 题目解析

    @TableField("difficulty")
    private String difficulty;  // 难度：EASY(简单)、MEDIUM(中等)、HARD(困难)

    @TableField("acceptRate")
    private String acceptRate;  // 通过率

    @TableField("submissionCount")
    private Integer submissionCount;  // 提交次数

    @TableField("createTime")
    private LocalDateTime createTime;

    @TableField("updateTime")
    private LocalDateTime updateTime;

    @TableField("isDelete")
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
} 