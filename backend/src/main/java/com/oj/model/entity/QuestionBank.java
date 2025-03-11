package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question_bank")
public class QuestionBank {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String difficulty;
    private String tags;
    private String permission;
    private Integer isHot;
    private Integer isRecommended;
    private Long userId;
    private Integer problemCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
//    @TableLogic
    private Integer isDelete;


}