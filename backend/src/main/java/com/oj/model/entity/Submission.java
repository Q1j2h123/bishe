package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("submission") // 映射到数据库中的 submission 表
public class Submission {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;
    private Long userId; // 用户ID
    private Long problemId; // 题目ID
    private String type; // 提交类型
    private String status; // 提交状态
    private LocalDateTime submissionTime; // 提交时间
    @TableField(value = "createTime", fill = FieldFill.INSERT) // 自动填充
    private LocalDateTime createTime;

    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE) // 自动填充
    private LocalDateTime updateTime;

    private Integer isDelete; // 是否删除
} 