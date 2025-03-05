package com.oj.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户注册请求")
public class UserRegisterRequest {
    @ApiModelProperty(value = "用户账号", required = true)
    private String userAccount;
    
    @ApiModelProperty(value = "用户密码", required = true)
    private String userPassword;
    
    @ApiModelProperty(value = "确认密码", required = true)
    private String checkPassword;
    
    @ApiModelProperty(value = "用户昵称", required = true)
    private String userName;
} 