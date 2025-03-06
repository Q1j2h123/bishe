package com.oj.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "用户登录请求")
public class UserLoginRequest {
    @NotBlank(message = "账号不能为空")
    @ApiModelProperty( "用户账号")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty("用户密码")
    private String userPassword;
} 