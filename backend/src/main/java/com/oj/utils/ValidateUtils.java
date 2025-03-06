package com.oj.utils;

import com.oj.common.ErrorCode;
import com.oj.constant.CommonConstant;
import com.oj.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ValidateUtils {
    
    public static void validateDifficulty(String difficulty) {
        if (StringUtils.isBlank(difficulty)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "难度不能为空");
        }
        if (!Arrays.asList(CommonConstant.DIFFICULTIES).contains(difficulty)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "难度值不合法");
        }
    }
    
    public static void validateProblemType(String type) {
        if (StringUtils.isBlank(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目类型不能为空");
        }
        if (!Arrays.asList(CommonConstant.PROBLEM_TYPES).contains(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目类型不合法");
        }
    }
    
    public static void validatePermission(String permission) {
        if (StringUtils.isBlank(permission)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "权限不能为空");
        }
        if (!Arrays.asList(CommonConstant.PERMISSIONS).contains(permission)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "权限值不合法");
        }
    }
} 