package com.oj.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户信息更新请求")
public class UserUpdateRequest {

    @ApiModelProperty(value = "用户名", example = "张三")
    private String userName;

    @ApiModelProperty(value = "用户头像", example = "https://example.com/avatar.jpg")
    private String userAvatar;

    @ApiModelProperty(value = "个人简介", example = "热爱编程，喜欢解决问题")
    private String userProfile;
} 