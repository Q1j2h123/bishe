package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user") // 映射到数据库中的 user 表
public class User implements Serializable {
    @TableId(type = IdType.AUTO) // 主键自增
     // 指定数据库字段名
    private Long id;
    
    @TableField("userAccount")  // 指定数据库字段名
    private String userAccount;
    
    @TableField("userPassword")
    private String userPassword;
    
    @TableField("userName")
    private String userName;
    @TableField("userAvatar")
    private String userAvatar;
    @TableField("userProfile")
    private String userProfile;
    @TableField("userRole")
    private String userRole;

    @TableField("isDelete")
//    @TableLogic
    private Integer isDelete;

    @TableField(value = "createTime", fill = FieldFill.INSERT) // 自动填充
    private LocalDateTime createTime;

    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE) // 自动填充
    private LocalDateTime updateTime;
}