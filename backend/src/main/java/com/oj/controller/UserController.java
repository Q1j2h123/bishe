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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import com.oj.model.request.UserLoginRequest;
import com.oj.model.request.UserRegisterRequest;
import com.oj.model.request.UserUpdateRequest;
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

    @PostMapping("/update")
    @ApiOperation(value = "更新用户信息", notes = "更新当前登录用户的个人信息")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                          HttpServletRequest request) {
        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.updateUser(userUpdateRequest, request);
        return ResultUtils.success(result);
    }

    @PostMapping("/upload/avatar")
    @ApiOperation(value = "上传用户头像", notes = "上传用户头像图片，返回图片URL")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 从当前登录用户信息中获取用户ID
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件为空");
        }

        try {
            // 获取文件原始名称
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名为空");
            }
            
            // 获取文件后缀
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // 生成新的文件名
            String fileName = UUID.randomUUID().toString() + suffix;
            
            // 获取应用根目录
            String rootPath = System.getProperty("user.dir");
            log.info("应用根目录: {}", rootPath);
            
            // 确保目录存在
            String uploadDir = rootPath + File.separator + "uploads" + File.separator + "avatars" + File.separator;
            log.info("上传目录: {}", uploadDir);
            
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean mkdirSuccess = dir.mkdirs();
                if (!mkdirSuccess) {
                    log.error("创建目录失败: {}", uploadDir);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建上传目录失败");
                }
                log.info("创建目录成功: {}", uploadDir);
            }
            
            // 保存文件
            String filePath = uploadDir + fileName;
            log.info("文件完整路径: {}", filePath);
            
            File dest = new File(filePath);
            file.transferTo(dest);
            
            // 记录日志
            log.info("文件上传成功，路径: {}", filePath);
            
            // 返回文件访问URL
            String fileUrl = "/api/uploads/avatars/" + fileName;
            log.info("文件访问URL: {}", fileUrl);
            
            // 同时更新用户头像信息
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setUserAvatar(fileUrl);
            boolean updateResult = userService.updateUser(updateRequest, request);
            log.info("用户头像信息更新结果: {}", updateResult);
            
            return ResultUtils.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败: " + e.getMessage());
        }
    }
} 