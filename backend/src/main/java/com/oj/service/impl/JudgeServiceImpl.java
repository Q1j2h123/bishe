package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oj.constant.JudgeConstant;
import com.oj.constant.SubmissionConstant;
import com.oj.exception.BusinessException;
import com.oj.mapper.ProgramSubmissionMapper;
import com.oj.mapper.SubmissionMapper;
import com.oj.mapper.TestcaseMapper;
import com.oj.mapper.UserProblemStatusMapper;
import com.oj.mapper.programProblemMapper;
import com.oj.model.dto.CompileResult;
import com.oj.model.dto.ExecutionResult;
import com.oj.model.dto.JudgeResult;
import com.oj.model.dto.TestcaseResult;
import com.oj.model.entity.ProgramProblem;
import com.oj.model.entity.ProgramSubmission;
import com.oj.model.entity.Submission;
import com.oj.model.entity.Testcase;
import com.oj.model.entity.UserProblemStatus;
import com.oj.service.CodeExecutor;
import com.oj.service.JudgeService;
import com.oj.service.ErrorProblemService;
// import com.oj.service.SubmissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评测服务实现
 */
@Service
@Slf4j
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private SubmissionMapper submissionMapper;
    
    @Resource
    private ProgramSubmissionMapper programSubmissionMapper;
    
    @Resource
    private programProblemMapper programProblemMapper;
    
    @Resource
    private TestcaseMapper testcaseMapper;
    
    @Resource
    private CodeExecutor codeExecutor;
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Resource
    private UserProblemStatusMapper userProblemStatusMapper;
    
    @Resource
    private ErrorProblemService errorProblemService;

    @Override
    @Async("judgeTaskExecutor")
    public void submitJudgeTask(Long submissionId) {
        log.info("提交评测任务: {}", submissionId);
        try {
            // 更新提交状态为评测中
            Submission submission = submissionMapper.selectById(submissionId);
            if (submission == null) {
                log.error("提交记录不存在: {}", submissionId);
                return;
            }
            
            submission.setStatus(JudgeConstant.STATUS_JUDGING);
            submissionMapper.updateById(submission);
            
            // 执行评测
            judge(submissionId);
        } catch (Exception e) {
            log.error("评测任务异常: {}", submissionId, e);
            // 更新为系统错误状态
            Submission submission = new Submission();
            submission.setId(submissionId);
            submission.setStatus(JudgeConstant.STATUS_SYSTEM_ERROR);
            submissionMapper.updateById(submission);
            
            // 更新错误信息
            ProgramSubmission programSubmission = new ProgramSubmission();
            programSubmission.setSubmissionId(submissionId);
            programSubmission.setErrorMessage("系统错误: " + e.getMessage());
            programSubmissionMapper.updateById(programSubmission);
        }
    }

    @Override
    public void judge(Long submissionId) {
        log.info("开始评测: {}", submissionId);
        
        // 获取提交记录
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(40000, "提交记录不存在");
        }
        
        // 获取程序代码提交详情
        ProgramSubmission programSubmission = programSubmissionMapper.selectById(submissionId);
        if (programSubmission == null) {
            throw new BusinessException(40000, "程序提交详情不存在");
        }
        
        // 获取问题详情
        ProgramProblem programProblem = programProblemMapper.selectById(submission.getProblemId());
        if (programProblem == null) {
            throw new BusinessException(40000, "程序题目不存在");
        }
        
        // 编译代码
        CompileResult compileResult = codeExecutor.compile(
                programSubmission.getCode(), 
                programSubmission.getLanguage());
        
        if (!compileResult.getSuccess()) {
            // 编译失败，更新状态
            JudgeResult judgeResult = JudgeResult.builder()
                    .status(JudgeConstant.STATUS_COMPILE_ERROR)
                    .errorMessage(compileResult.getErrorMessage())
                    .build();
            saveJudgeResult(submissionId, judgeResult);
            return;
        }
        
        // 获取测试用例
        LambdaQueryWrapper<Testcase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Testcase::getProblemId, submission.getProblemId())
                   .eq(Testcase::getIsDelete, 0);
        List<Testcase> testcases = testcaseMapper.selectList(queryWrapper);
        
        if (testcases.isEmpty()) {
            log.warn("题目没有测试用例: {}", submission.getProblemId());
            // 没有测试用例默认通过
            JudgeResult judgeResult = JudgeResult.builder()
                    .status(JudgeConstant.STATUS_ACCEPTED)
                    .passedCount(0)
                    .totalCount(0)
                    .build();
            saveJudgeResult(submissionId, judgeResult);
            return;
        }
        
        // 执行所有测试用例
        List<TestcaseResult> testcaseResults = new ArrayList<>();
        long maxExecutionTime = 0;
        long maxMemoryUsage = 0;
        int passedCount = 0;
        
        // 获取时间和内存限制 - 将Integer转为Long类型
        Long timeLimit = programProblem.getTimeLimit() != null ? 
                Long.valueOf(programProblem.getTimeLimit()) : JudgeConstant.DEFAULT_TIME_LIMIT;
        
        // 将MB转为KB（如果memoryLimit单位是MB的话）
        Long memoryLimit = programProblem.getMemoryLimit() != null ? 
                Long.valueOf(programProblem.getMemoryLimit() * 1024) : JudgeConstant.DEFAULT_MEMORY_LIMIT_KB;
        
        for (Testcase testcase : testcases) {
            ExecutionResult executionResult = codeExecutor.execute(
                    compileResult.getCompiledFilePath(),
                    testcase.getInput(),
                    timeLimit,
                    memoryLimit);
            
            // 创建测试用例结果
            TestcaseResult testcaseResult = TestcaseResult.builder()
                    .testcaseId(testcase.getId())
                    .input(testcase.getInput())
                    .expectedOutput(testcase.getExpectedOutput())
                    .actualOutput(executionResult.getOutput())
                    .executionTime(executionResult.getExecutionTime())
                    .memoryUsage(executionResult.getMemoryUsage())
                    .errorMessage(executionResult.getErrorMessage())
                    .build();
            
            // 检查执行状态
            if (!executionResult.getSuccess()) {
                // 执行失败（超时、内存超限、运行时错误等）
                testcaseResult.setPassed(false);
                testcaseResults.add(testcaseResult);
                
                // 找到第一个失败的测试用例就返回结果
                JudgeResult judgeResult = JudgeResult.builder()
                        .status(executionResult.getStatus())
                        .executionTime(executionResult.getExecutionTime())
                        .memoryUsage(executionResult.getMemoryUsage())
                        .errorMessage(executionResult.getErrorMessage())
                        .testcaseResults(testcaseResults)
                        .passedCount(passedCount)
                        .totalCount(testcases.size())
                        .build();
                saveJudgeResult(submissionId, judgeResult);
                return;
            }
            
            // 比较输出结果
            boolean passed = compareOutput(executionResult.getOutput(), testcase.getExpectedOutput());
            testcaseResult.setPassed(passed);
            
            if (passed) {
                passedCount++;
            }
            
            // 更新最大执行时间和内存使用
            maxExecutionTime = Math.max(maxExecutionTime, executionResult.getExecutionTime());
            maxMemoryUsage = Math.max(maxMemoryUsage, executionResult.getMemoryUsage());
            
            testcaseResults.add(testcaseResult);
        }
        
        // 全部测试用例执行完成，判断最终状态
        String finalStatus = passedCount == testcases.size() ? 
                JudgeConstant.STATUS_ACCEPTED : JudgeConstant.STATUS_WRONG_ANSWER;
        
        JudgeResult judgeResult = JudgeResult.builder()
                .status(finalStatus)
                .executionTime(maxExecutionTime)
                .memoryUsage(maxMemoryUsage)
                .testcaseResults(testcaseResults)
                .passedCount(passedCount)
                .totalCount(testcases.size())
                .build();
        
        saveJudgeResult(submissionId, judgeResult);
    }

    @Override
    public void saveJudgeResult(Long submissionId, JudgeResult result) {
        try {
            log.info("保存评测结果: {}, 状态: {}", submissionId, result.getStatus());
            
            // 获取完整提交记录，以便访问userId和problemId
            Submission existingSubmission = submissionMapper.selectById(submissionId);
            if (existingSubmission == null) {
                log.error("提交记录不存在: {}", submissionId);
                return;
            }
            
            // 更新提交状态
            Submission submission = new Submission();
            submission.setId(submissionId);
            submission.setStatus(result.getStatus());
            submissionMapper.updateById(submission);
            
            // 更新程序提交详情
            ProgramSubmission programSubmission = new ProgramSubmission();
            programSubmission.setSubmissionId(submissionId);
            programSubmission.setErrorMessage(result.getErrorMessage());
            
            // 序列化测试用例结果为JSON
            if (result.getTestcaseResults() != null && !result.getTestcaseResults().isEmpty()) {
                String testcaseResultsJson = objectMapper.writeValueAsString(result.getTestcaseResults());
                programSubmission.setTestcaseResults(testcaseResultsJson);
            }
            
            programSubmissionMapper.updateById(programSubmission);
            
            // 更新用户题目状态
            updateUserProblemStatus(
                    existingSubmission.getUserId(), 
                    existingSubmission.getProblemId(), 
                    result.getStatus());
                    
            // 如果评测结果不是ACCEPTED，则添加到错题本
            if (!JudgeConstant.STATUS_ACCEPTED.equals(result.getStatus())) {
                log.info("题目评测未通过，添加到错题本: userId={}, problemId={}", 
                    existingSubmission.getUserId(), existingSubmission.getProblemId());
                errorProblemService.addErrorProblem(
                    existingSubmission.getUserId(), 
                    existingSubmission.getProblemId());
            }
        } catch (Exception e) {
            log.error("保存评测结果失败: {}", submissionId, e);
        }
    }
    
    /**
     * 比较输出结果（忽略空白符差异）
     */
    private boolean compareOutput(String actual, String expected) {
        if (actual == null || expected == null) {
            return actual == expected;
        }
        
        // 统一行尾，清理多余空白
        actual = normalizeOutput(actual);
        expected = normalizeOutput(expected);
        
        return actual.equals(expected);
    }
    
    /**
     * 标准化输出文本（移除多余空白符，统一换行符）
     */
    private String normalizeOutput(String output) {
        if (output == null) {
            return "";
        }
        
        // 按行分割
        String[] lines = output.split("\\r?\\n");
        
        // 处理每行（去除前后空白）- 使用Java 8兼容的方式
        return Arrays.stream(lines)
                .map(String::trim)
                .collect(Collectors.joining("\n"));
    }

    /**
     * 更新用户题目状态，实现"只升不降"的逻辑
     * 状态优先级: SOLVED > ATTEMPTED > UNSOLVED
     */
    private void updateUserProblemStatus(Long userId, Long problemId, String status) {
        try {
            log.info("开始更新用户题目状态: userId={}, problemId={}, status={}", userId, problemId, status);
            
            // 查询是否已有记录
            LambdaQueryWrapper<UserProblemStatus> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserProblemStatus::getUserId, userId)
                    .eq(UserProblemStatus::getProblemId, problemId);
            
            UserProblemStatus userProblemStatus = userProblemStatusMapper.selectOne(queryWrapper);
            
            // 检查更新条件
            boolean shouldUpdate = true;
            
            if (userProblemStatus == null) {
                // 没有记录，创建新记录
                log.info("用户题目状态不存在，创建新记录");
                userProblemStatus = new UserProblemStatus();
                userProblemStatus.setUserId(userId);
                userProblemStatus.setProblemId(problemId);
                userProblemStatus.setStatus(status);
                userProblemStatusMapper.insert(userProblemStatus);
                return;
            } else {
                log.info("找到现有状态: {}", userProblemStatus);
                String currentStatus = userProblemStatus.getStatus();
                
                // 如果当前状态是已解决，则不再更新
                if (SubmissionConstant.USER_STATUS_SOLVED.equals(currentStatus)) {
                    log.info("题目已经是SOLVED状态，保持不变");
                    shouldUpdate = false;
                } 
                // 如果要更新为ATTEMPTED，但当前已经是更高级别的状态，则不更新
                else if (SubmissionConstant.USER_STATUS_ATTEMPTED.equals(status) && 
                        SubmissionConstant.USER_STATUS_SOLVED.equals(currentStatus)) {
                    log.info("当前状态为SOLVED，新状态为ATTEMPTED，保持不变");
                    shouldUpdate = false;
                }
                // 其他情况：从UNSOLVED升级到任何状态，或从ATTEMPTED升级到SOLVED，都更新
            }
            
            if (shouldUpdate) {
                log.info("更新用户题目状态: {} -> {}", userProblemStatus.getStatus(), status);
                userProblemStatus.setStatus(status);
                userProblemStatus.setLastSubmitTime(LocalDateTime.now());
                userProblemStatusMapper.updateById(userProblemStatus);
            }
        } catch (Exception e) {
            log.error("更新用户题目状态失败", e);
        }
    }
} 