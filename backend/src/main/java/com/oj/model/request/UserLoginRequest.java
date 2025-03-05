package com.oj.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户登录请求")
public class UserLoginRequest {
    @ApiModelProperty(value = "用户账号", required = true)
    private String userAccount;
    
    @ApiModelProperty(value = "用户密码", required = true)
    private String userPassword;
} 