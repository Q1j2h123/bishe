package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户题目状态实体类
 */
@Data
@TableName("user_problem_status")
public class UserProblemStatus {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 题目ID
     */
    private Long problemId;
    
    /**
     * 状态：UNSOLVED(未解决)、ATTEMPTED(尝试过)、SOLVED(已解决)
     */
    private String status;
    
    /**
     * 最后提交时间
     */
    @TableField("lastSubmitTime")
    private LocalDateTime lastSubmitTime;
    
    /**
     * 创建时间
     */
    @TableField(value="createTime",fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value="updateTime",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}