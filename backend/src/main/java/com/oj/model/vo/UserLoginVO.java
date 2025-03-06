package com.oj.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户登录视图对象")
public class UserLoginVO {
    @Schema(description = "用户信息")
    private UserVO user;
    
    @Schema(description = "token")
    private String token;
} 