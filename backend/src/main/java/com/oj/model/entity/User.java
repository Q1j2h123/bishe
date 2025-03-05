package com.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("user")
@ApiModel(description = "用户实体")
public class User implements Serializable {

    @ApiModelProperty("用户ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户账号")
    @TableField("userAccount")
    private String userAccount;

    @ApiModelProperty("用户密码")
    @TableField("userPassword")
    private String userPassword;

    @ApiModelProperty("微信开放平台id")
    @TableField("unionId")
    private String unionId;

    @ApiModelProperty("微信公众号openId")
    @TableField("mpOpenId")
    private String mpOpenId;

    @ApiModelProperty("用户昵称")
    @TableField("userName")
    private String userName;

    @ApiModelProperty("用户头像")
    @TableField("userAvatar")
    private String userAvatar;

    @ApiModelProperty("用户简介")
    @TableField("userProfile")
    private String userProfile;

    @ApiModelProperty("用户角色：user/admin")
    @TableField("userRole")
    private String userRole;

    @ApiModelProperty("是否删除")
    @TableField("isDelete")
    @TableLogic
    private Integer isDelete;

    @ApiModelProperty("创建时间")
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty("更新时间")
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
} 