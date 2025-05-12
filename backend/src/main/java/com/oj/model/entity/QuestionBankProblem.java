package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question_bank_problem")
public class QuestionBankProblem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long questionBankId;
    
    private Long problemId;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
//    @TableLogic
    private Integer isDelete;


}