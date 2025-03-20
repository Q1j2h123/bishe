package com.oj.controller.admin;

import com.oj.annotation.AuthCheck;
import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.constant.UserConstant;
import com.oj.exception.BusinessException;
import com.oj.model.entity.User;
import com.oj.service.ImportService;
import com.oj.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * 题目导入控制器
 */
@RestController
@RequestMapping("/api/admin/import")
@Api(tags = "题目导入接口")
@Slf4j
public class ProblemImportController {

    @Resource
    private ImportService importService;

    @Resource
    private UserService userService;

    @PostMapping("/choice")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "批量导入选择题", notes = "从Excel文件导入选择题，需要管理员权限")
    public BaseResponse<Map<String, Object>> importChoiceProblems(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        
        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls") && !fileName.endsWith(".csv"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式不正确，请上传Excel或CSV文件");
        }
        
        try {
            User loginUser = userService.getLoginUser(request);
            Map<String, Object> result = importService.importChoiceProblems(file, loginUser.getId());
            return ResultUtils.success(result);
        } catch (IOException e) {
            log.error("导入选择题出错", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导入失败，请检查文件格式: " + e.getMessage());
        }
    }

    @PostMapping("/judge")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "批量导入判断题", notes = "从Excel文件导入判断题，需要管理员权限")
    public BaseResponse<Map<String, Object>> importJudgeProblems(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        
        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls") && !fileName.endsWith(".csv"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式不正确，请上传Excel或CSV文件");
        }
        
        try {
            User loginUser = userService.getLoginUser(request);
            Map<String, Object> result = importService.importJudgeProblems(file, loginUser.getId());
            return ResultUtils.success(result);
        } catch (IOException e) {
            log.error("导入判断题出错", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导入失败，请检查文件格式: " + e.getMessage());
        }
    }

    @PostMapping("/program")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "批量导入编程题", notes = "从Excel文件导入编程题，需要管理员权限")
    public BaseResponse<Map<String, Object>> importProgramProblems(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        
        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls") && !fileName.endsWith(".csv"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式不正确，请上传Excel或CSV文件");
        }
        
        try {
            User loginUser = userService.getLoginUser(request);
            Map<String, Object> result = importService.importProgramProblems(file, loginUser.getId());
            return ResultUtils.success(result);
        } catch (IOException e) {
            log.error("导入编程题出错", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导入失败，请检查文件格式: " + e.getMessage());
        }
    }
} 