package com.oj.controller;

import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.exception.BusinessException;
import com.oj.model.entity.User;
import com.oj.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.oj.model.request.UserLoginRequest;
import com.oj.model.request.UserRegisterRequest;
import com.oj.model.vo.UserLoginVO;
import com.oj.model.vo.UserVO;

import com.oj.utils.JwtUtils;

import com.oj.model.dto.UserDTO;


@RestController
@RequestMapping("/api/user")
@Api(tags = "用户管理接口")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "注册新用户，返回用户id")
    public BaseResponse<Long> userRegister(@ApiParam(value = "注册信息", required = true) @RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return new BaseResponse<>(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userName = userRegisterRequest.getUserName();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userName)) {
            return new BaseResponse<>(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, userName);
        return BaseResponse.success(result);
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "用户登录并返回登录信息")
    public BaseResponse<UserLoginVO> userLogin(@ApiParam(value = "登录信息", required = true) @RequestBody @Valid UserLoginRequest userLoginRequest,
                                             HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
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
    @ApiOperation(value = "用户登出", notes = "退出登录")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    @ApiOperation(value = "获取当前用户", notes = "获取当前登录用户信息")
    public BaseResponse<UserDTO> getCurrentUser(HttpServletRequest request) {
        UserDTO user = userService.getCurrentUser(request);
        return ResultUtils.success(user);
    }
} 