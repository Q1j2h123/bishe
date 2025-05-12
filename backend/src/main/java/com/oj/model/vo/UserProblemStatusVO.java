package com.oj.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户题目状态视图对象
 */
@Data
public class UserProblemStatusVO {
    
    /**
     * 题目ID
     */
    private Long problemId;
    
    /**
     * 题目标题
     */
    private String problemTitle;
    
    /**
     * 题目类型
     */
    private String type;
    
    /**
     * 状态：UNSOLVED(未解决)、ATTEMPTED(尝试过)、SOLVED(已解决)
     */
    private String status;
    
    /**
     * 最后提交时间
     */
    private LocalDateTime lastSubmitTime;
    
    /**
     * 难度
     */
    private String difficulty;
    
    /**
     * 标签列表
     */
    private java.util.List<String> tags;
} 