package com.oj.controller;

import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.model.entity.User;
import com.oj.model.request.UserLoginRequest;
import com.oj.model.request.UserRegisterRequest;
import com.oj.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user")
@Api(tags = "用户接口")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "注册新用户")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest registerRequest) {
        if (registerRequest == null) {
            return new BaseResponse<>(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = registerRequest.getUserAccount();
        String userPassword = registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();
        String userName = registerRequest.getUserName();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userName)) {
            return new BaseResponse<>(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, userName);
        return BaseResponse.success(result);
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "用户登录并返回用户信息")
    public BaseResponse<User> login(@RequestBody UserLoginRequest loginRequest) {
        if (loginRequest == null) {
            return new BaseResponse<>(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return new BaseResponse<>(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword);
        return BaseResponse.success(user);
    }

    @PostMapping("/logout")
    @ApiOperation(value = "用户注销", notes = "用户退出登录")
    public BaseResponse<Boolean> logout() {
        return BaseResponse.success(true);
    }

    @GetMapping("/current")
    @ApiOperation(value = "获取当前登录用户", notes = "获取当前登录用户信息")
    public BaseResponse<User> getCurrentUser() {
        User loginUser = userService.getLoginUser(null);
        return BaseResponse.success(loginUser);
    }
} 