package com.oj.controller;

import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.exception.BusinessException;
import com.oj.model.entity.User;
import com.oj.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import com.oj.model.request.UserLoginRequest;
import com.oj.model.request.UserRegisterRequest;
import com.oj.model.vo.UserLoginVO;
import com.oj.model.vo.UserVO;

import com.oj.utils.JwtUtils;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户接口", description = "用户管理相关接口")
@ApiSupport(author = "OJ System")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户")
    @ApiOperationSupport(order = 1)
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
    @Operation(summary = "用户登录", description = "用户登录并返回用户信息")
    @ApiOperationSupport(order = 2)
    public BaseResponse<UserLoginVO> login(@RequestBody @Valid UserLoginRequest loginRequest) {
        if (loginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword);
        // 生成token
        String token = jwtUtils.generateToken(user.getId());
        // 构建返回对象
        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setUser(userService.getUserVO(user.getId()));
        loginVO.setToken(token);
        return ResultUtils.success(loginVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户注销", description = "用户退出登录")
    @ApiOperationSupport(order = 3)
    public BaseResponse<Boolean> logout() {
        return BaseResponse.success(true);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户信息")
    @ApiOperationSupport(order = 4)
    public BaseResponse<UserVO> getCurrentUser() {
        User loginUser = userService.getLoginUser(null);
        return ResultUtils.success(userService.getUserVO(loginUser.getId()));
    }
} 