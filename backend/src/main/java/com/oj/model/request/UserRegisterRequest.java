package com.oj.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "用户注册请求")
public class UserRegisterRequest {
    @NotBlank(message = "账号不能为空")
    @ApiModelProperty( "用户账号")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty("用户密码")
    private String userPassword;

    @NotBlank(message = "确认密码不能为空")
    @ApiModelProperty("确认密码")
    private String checkPassword;

    @NotBlank(message = "昵称不能为空")
    @ApiModelProperty( "用户昵称")
    private String userName;
} 