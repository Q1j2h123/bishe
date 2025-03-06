package com.oj.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户视图对象")
public class UserVO {
    @ApiModelProperty(value = "用户ID")
    private Long id;

    @ApiModelProperty(value = "用户账号")
    private String userAccount;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    @ApiModelProperty(value = "用户简介")
    private String userProfile;

    @ApiModelProperty(value = "用户角色")
    private String userRole;
} 