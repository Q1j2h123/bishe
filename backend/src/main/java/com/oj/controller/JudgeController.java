package com.oj.controller;

import com.oj.annotation.AuthCheck;
import com.oj.common.BaseResponse;
import com.oj.common.ResultUtils;
import com.oj.common.UserContext;
import com.oj.constant.UserConstant;
import com.oj.mapper.ProgramSubmissionMapper;
import com.oj.model.dto.CompileResult;
import com.oj.model.dto.ExecutionResult;
import com.oj.model.dto.JudgeResult;
import com.oj.model.dto.TestCaseDTO;
import com.oj.model.entity.ProgramProblem;
import com.oj.model.entity.ProgramSubmission;
import com.oj.model.entity.Submission;
import com.oj.model.request.CodeRunRequest;
import com.oj.service.CodeExecutor;
import com.oj.service.JudgeService;
import com.oj.service.SubmissionService;
import com.oj.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 判题控制器
 */
@RestController
@RequestMapping("/api/judge")
@Api(tags = "判题接口")
@Slf4j
public class JudgeController {
    
    @Resource
    private JudgeService judgeService;
    
    @Resource
    private SubmissionService submissionService;
    
    @Resource
    private UserService userService;

    @Resource
    private ProgramSubmissionMapper programSubmissionMapper;
    
    @Resource
    private CodeExecutor codeExecutor;
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Resource
    private com.oj.mapper.programProblemMapper programProblemMapper;
    
    /**
     * 运行代码（不保存提交记录）
     */
    @PostMapping("/run")
    @ApiOperation("运行代码")
    public BaseResponse<Map<String, Object>> runCode(@RequestBody CodeRunRequest request, HttpServletRequest httpRequest) {
        if (request.getProblemId() == null) {
            return ResultUtils.error(40000, "题目ID不能为空");
        }
        
        if (request.getCode() == null || request.getCode().isEmpty()) {
            return ResultUtils.error(40000, "代码不能为空");
        }
        
        if (request.getLanguage() == null || request.getLanguage().isEmpty()) {
            return ResultUtils.error(40000, "编程语言不能为空");
        }
        
        // 从当前登录用户信息中获取用户ID
        Long userId = UserContext.getUser().getId();
        
        try {
            // 获取题目信息
            ProgramProblem programProblem = programProblemMapper.selectById(request.getProblemId());
            if (programProblem == null) {
                return ResultUtils.error(40000, "编程题不存在");
            }
            
            // 编译代码
            CompileResult compileResult = codeExecutor.compile(request.getCode(), request.getLanguage());
            
            if (!compileResult.getSuccess()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("errorMessage", compileResult.getErrorMessage());
                return ResultUtils.success(result);
            }
            
            // 从题目中获取示例测试用例（仅运行第一个测试用例）
            List<TestCaseDTO> testCases = new ArrayList<>();
            try {
                if (programProblem.getTestCases() != null && !programProblem.getTestCases().isEmpty()) {
                    testCases = objectMapper.readValue(
                        programProblem.getTestCases(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TestCaseDTO.class)
                    );
                }
            } catch (Exception e) {
                log.error("解析测试用例失败", e);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("errorMessage", "解析测试用例失败: " + e.getMessage());
                return ResultUtils.success(result);
            }
            
            if (testCases.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("errorMessage", "题目没有测试用例");
                return ResultUtils.success(result);
            }
            
            // 获取第一个测试用例
            TestCaseDTO testCase = testCases.get(0);
            
            // 获取时间和内存限制
            Long timeLimit = programProblem.getTimeLimit() != null ? 
                    Long.valueOf(programProblem.getTimeLimit()) : 1000L;
            
            // 将MB转为KB
            Long memoryLimit = programProblem.getMemoryLimit() != null ? 
                    Long.valueOf(programProblem.getMemoryLimit() * 1024) : 262144L; // 默认256MB
            
            // 执行代码
            ExecutionResult executionResult = codeExecutor.execute(
                    compileResult.getCompiledFilePath(),
                    testCase.getInput(),
                    timeLimit,
                    memoryLimit);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", executionResult.getSuccess());
            result.put("executionTime", executionResult.getExecuteTime());
            result.put("memoryUsage", executionResult.getMemoryUsage() / 1024); // 转换为MB
            result.put("output", executionResult.getOutput());
            result.put("errorMessage", executionResult.getErrorMessage());
            result.put("expectedOutput", testCase.getOutput());
            
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("运行代码异常", e);
            return ResultUtils.error(50000, "运行代码失败: " + e.getMessage());
        }
    }

    @PostMapping("/manually/{id}")
    @ApiOperation("手动触发评测(管理员)")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> manualJudge(@PathVariable("id") Long submissionId, HttpServletRequest request) {
        if (submissionId == null) {
            return ResultUtils.error(40000, "提交ID不能为空");
        }
        
        // 检查提交记录是否存在
        Submission submission = submissionService.getById(submissionId);
        if (submission == null) {
            return ResultUtils.error(40000, "提交记录不存在");
        }
        
        // 提交评测任务
        judgeService.submitJudgeTask(submissionId);
        
        return ResultUtils.success(true);
    }
    
    @GetMapping("/status/{id}")
    @ApiOperation("获取评测状态")
    public BaseResponse<String> getJudgeStatus(@PathVariable("id") Long submissionId, HttpServletRequest request) {
        if (submissionId == null) {
            return ResultUtils.error(40000, "提交ID不能为空");
        }
        
        // 检查提交记录是否存在
        Submission submission = submissionService.getById(submissionId);
        if (submission == null) {
            return ResultUtils.error(40000, "提交记录不存在");
        }
        
        // 检查权限（只有管理员和提交者本人可以查看）
        Long loginUserId = UserContext.getUser().getId();
        if (!UserConstant.ADMIN_ROLE.equals(userService.getById(loginUserId).getUserRole()) 
                && !submission.getUserId().equals(loginUserId)) {
            return ResultUtils.error(40300, "无权限查看该记录");
        }
        
        return ResultUtils.success(submission.getStatus());
    }
    
    @GetMapping("/result/{id}")
    @ApiOperation("获取评测结果")
    public BaseResponse<JudgeResult> getJudgeResult(@PathVariable("id") Long submissionId, HttpServletRequest request) {
        if (submissionId == null) {
            return ResultUtils.error(40000, "提交ID不能为空");
        }
        
        // 检查提交记录是否存在
        Submission submission = submissionService.getById(submissionId);
        if (submission == null) {
            return ResultUtils.error(40000, "提交记录不存在");
        }
        
        // 检查权限（只有管理员和提交者本人可以查看）
        Long loginUserId = UserContext.getUser().getId();
        if (!UserConstant.ADMIN_ROLE.equals(userService.getById(loginUserId).getUserRole()) 
                && !submission.getUserId().equals(loginUserId)) {
            return ResultUtils.error(40300, "无权限查看该记录");
        }
        
        // 检查提交类型
        if (!"PROGRAM".equals(submission.getType())) {
            return ResultUtils.error(40000, "非编程题提交，无法获取详细结果");
        }
        
        // 获取程序提交详情
        ProgramSubmission programSubmission = programSubmissionMapper.selectById(submissionId);
        if (programSubmission == null) {
            return ResultUtils.error(40000, "提交详情不存在");
        }
        
        // 构建评测结果
        JudgeResult result = new JudgeResult();
        result.setStatus(submission.getStatus());
        result.setExecuteTime(programSubmission.getExecuteTime() != null ? programSubmission.getExecuteTime().longValue() : 0L);
        result.setMemoryUsage(programSubmission.getMemoryUsage() != null ? programSubmission.getMemoryUsage().longValue() : 0L);
        result.setErrorMessage(programSubmission.getErrorMessage());
        
        // 如果有测试用例结果，解析测试用例结果
        // 这里简化处理，实际应该从JSON反序列化
        
        return ResultUtils.success(result);
    }
} 