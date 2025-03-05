package com.oj.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserRegisterRequest {
    @NotBlank(message = "账号不能为空")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    private String userPassword;

    @NotBlank(message = "确认密码不能为空")
    private String checkPassword;

    @NotBlank(message = "昵称不能为空")
    private String userName;
} 