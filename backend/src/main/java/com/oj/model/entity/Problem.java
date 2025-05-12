package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName("problem")
public class Problem {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 题目类型
     */
    private String type;

    /**
     * 岗位类型
     */
    @TableField("jobType")
    private String jobType;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 标签
     */
    private String tags;

    /**
     * 通过率
     */
    @TableField("acceptRate")
    private String acceptRate;

    /**
     * 提交次数
     */
    @TableField("submissionCount")
    private Integer submissionCount;

    /**
     * 创建者id
     */
    @TableField("userId")
    private Long userId;

    /**
     * 状态（UNSOLVED-未解决，ATTEMPTED-尝试过，SOLVED-已解决）
     */
    private String status;

    /**
     * 是否删除
     */
    @TableField("isDelete")
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("updateTime")
    private LocalDateTime updateTime;

//    /**
//     * 每日一题日期（格式：yyyy-MM-dd）
//     */
//    private String dailyDate;
}
 



