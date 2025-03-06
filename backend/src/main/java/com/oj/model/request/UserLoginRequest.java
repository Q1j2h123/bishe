package com.oj.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "用户登录请求")
public class UserLoginRequest {
    @NotBlank(message = "账号不能为空")
    @Schema(description = "用户账号")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "用户密码")
    private String userPassword;
} 