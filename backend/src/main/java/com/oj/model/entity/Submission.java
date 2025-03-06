package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private Integer isDelete; // 是否删除
} 