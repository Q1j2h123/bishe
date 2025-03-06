package com.oj.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户登录视图对象")
public class UserLoginVO {
    @ApiModelProperty(value = "用户信息")
    private UserVO user;
    
    @ApiModelProperty(value = "token")
    private String token;
} 