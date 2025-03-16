package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户题目草稿实体类
 */
@Data
@TableName("draft_solution")
public class DraftSolution {
    
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
     * 题目类型: CHOICE, JUDGE, PROGRAM
     */
    private String type;
    
    /**
     * 草稿内容(选择题/判断题的选择或编程题的代码)
     */
    private String content;
    
    /**
     * 编程语言(编程题适用)
     */
    private String language;
    
    /**
     * 最后保存时间
     */
    private LocalDateTime lastSaveTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 