package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oj.constant.JudgeConstant;
import com.oj.constant.SubmissionConstant;
import com.oj.exception.BusinessException;
import com.oj.mapper.ProgramSubmissionMapper;
import com.oj.mapper.SubmissionMapper;
import com.oj.mapper.UserProblemStatusMapper;
import com.oj.mapper.programProblemMapper;
import com.oj.model.dto.CompileResult;
import com.oj.model.dto.ExecutionResult;
import com.oj.model.dto.JudgeResult;
import com.oj.model.dto.TestcaseResult;
import com.oj.model.dto.TestCaseDTO;
import com.oj.model.entity.ProgramProblem;
import com.oj.model.entity.ProgramSubmission;
import com.oj.model.entity.Submission;
import com.oj.model.entity.UserProblemStatus;
import com.oj.service.CodeExecutor;
import com.oj.service.JudgeService;
import com.oj.service.ErrorProblemService;
import com.oj.service.UserProblemStatusService;
import com.oj.util.JudgeContextHolder;
// import com.oj.service.SubmissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private ErrorProblemService errorProblemService;
    
    @Resource
    private UserProblemStatusService userProblemStatusService;
    
    @Resource
    @Qualifier("dockerCodeExecutor")
    private CodeExecutor codeExecutor;
    
    @Resource
    private AICodeExecutor aiCodeExecutor;
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Resource
    private UserProblemStatusMapper userProblemStatusMapper;
    
    @Resource
    private Environment env;
    
    @Value("${judge.use-ai-executor:false}")
    private boolean useAIExecutor;
    
    // 添加线程池用于评测任务
    private ExecutorService judgeThreadPool;
    private ExecutorService commonThreadPool;
    
    @PostConstruct
    public void init() {
        judgeThreadPool = Executors.newFixedThreadPool(5);
        commonThreadPool = Executors.newFixedThreadPool(2);
        log.info("评测服务初始化完成，AI评测器状态: {}", useAIExecutor ? "启用" : "禁用");
    }

    @Override
    public void submitJudgeTask(Long submissionId) {
        log.info("提交评测任务: {}", submissionId);
        
        // 先设置当前线程的提交ID，以便在异步任务中能够获取
        JudgeContextHolder.setSubmissionId(submissionId);
        
        // 这里不要清除，因为我们要在异步任务中使用
        try {
            // 更新提交状态为JUDGING
            Submission submission = submissionMapper.selectById(submissionId);
            if (submission != null) {
                submission.setStatus(JudgeConstant.STATUS_JUDGING);
                submissionMapper.updateById(submission);
                
                // 异步执行评测任务
                if (!useAIExecutor) {
                    // 如果禁用了AI判题，使用常规线程池
                    commonThreadPool.execute(() -> {
                        try {
                            judge(submissionId);
                        } catch (Exception e) {
                            log.error("评测任务执行异常", e);
                        } finally {
                            // 清除当前线程的提交ID
                            JudgeContextHolder.clear();
                        }
                    });
                } else {
                    // 使用专用评测线程池
                    judgeThreadPool.execute(() -> {
                        try {
                            judge(submissionId);
                        } catch (Exception e) {
                            log.error("评测任务执行异常", e);
                        } finally {
                            // 清除当前线程的提交ID
                            JudgeContextHolder.clear();
                        }
                    });
                }
            } else {
                log.error("提交记录不存在: {}", submissionId);
            }
        } catch (Exception e) {
            log.error("提交评测任务失败", e);
            // 清除当前线程的提交ID
            JudgeContextHolder.clear();
        }
    }

    @Override
    public void judge(Long submissionId) {
        log.info("开始评测: {}", submissionId);
        
        // 设置当前线程的提交ID
        JudgeContextHolder.setSubmissionId(submissionId);
        
        try {
            // 1. 获取提交记录
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
                log.error("提交记录不存在: {}", submissionId);
                return;
            }
            
            // 2. 获取提交详情
            ProgramSubmission programSubmission = programSubmissionMapper.selectOne(
                    new LambdaQueryWrapper<ProgramSubmission>()
                            .eq(ProgramSubmission::getSubmissionId, submissionId)
            );
        if (programSubmission == null) {
                log.error("编程题提交详情不存在: {}", submissionId);
                return;
            }
            
            // 3. 获取题目详情
            ProgramProblem problem = programProblemMapper.selectById(submission.getProblemId());
            if (problem == null) {
                log.error("题目不存在: {}", submission.getProblemId());
                return;
            }
            
            // 保存用户代码到本地文件，以便AI评判使用
            saveUserCode(submissionId, programSubmission.getCode(), programSubmission.getLanguage());
            
            // 选择代码执行器
            CodeExecutor executor = useAIExecutor ? aiCodeExecutor : codeExecutor;
            
            // 4. 编译代码
            CompileResult compileResult = executor.compile(programSubmission.getCode(), programSubmission.getLanguage());
            
            // 5. 编译失败
        if (!compileResult.getSuccess()) {
                log.warn("编译失败: {}", compileResult.getErrorMessage());
                
            JudgeResult judgeResult = JudgeResult.builder()
                    .status(JudgeConstant.STATUS_COMPILE_ERROR)
                    .errorMessage(compileResult.getErrorMessage())
                    .build();
                
            saveJudgeResult(submissionId, judgeResult);
            return;
        }
        
            // 6. 获取测试用例
            List<TestCaseDTO> testCases = getTestCases(problem);
            
            // 7. 执行测试用例
            List<TestcaseResult> results = new ArrayList<>();
            boolean allPassed = true;
            long totalTime = 0;
            long maxMemory = 0;
            
            for (int i = 0; i < testCases.size(); i++) {
                TestCaseDTO testCase = testCases.get(i);
                
                // 执行代码
                ExecutionResult executionResult = executor.execute(
                        compileResult.getCompiledFilePath(),
                        testCase.getInput(),
                        problem.getTimeLimit() != null ? problem.getTimeLimit().longValue() : null,
                        problem.getMemoryLimit() != null ? problem.getMemoryLimit().longValue() * 1024 : null
                );
                
                // 更新统计信息
                if (executionResult.getExecuteTime() != null) {
                    totalTime += executionResult.getExecuteTime();
                }
                if (executionResult.getMemoryUsage() != null && executionResult.getMemoryUsage() > maxMemory) {
                    maxMemory = executionResult.getMemoryUsage();
                }
                
                // 检查运行结果
                if (!executionResult.getSuccess()) {
                    log.warn("执行失败: {}", executionResult.getErrorMessage());
                    
            JudgeResult judgeResult = JudgeResult.builder()
                            .status(executionResult.getStatus())
                            .errorMessage(executionResult.getErrorMessage())
                            .executeTime(executionResult.getExecuteTime())
                            .memoryUsage(executionResult.getMemoryUsage())
                            .testcaseResults(results)
                            .passedCount(results.size())
                            .totalCount(testCases.size())
                    .build();
                    
            saveJudgeResult(submissionId, judgeResult);
            return;
        }
        
                // 记录测试用例结果
                TestcaseResult testcaseResult = new TestcaseResult();
                testcaseResult.setTestcaseId(testCase.getId());
                
                // 比较输出结果
                boolean passed = compareOutput(executionResult.getOutput(), testCase.getOutput());
                allPassed = allPassed && passed;
                
                testcaseResult.setPassed(passed);
                testcaseResult.setInput(testCase.getInput());
                testcaseResult.setExpectedOutput(testCase.getOutput());
                testcaseResult.setActualOutput(executionResult.getOutput());
                testcaseResult.setExecutionTime(executionResult.getExecuteTime());
                testcaseResult.setMemoryUsage(executionResult.getMemoryUsage());
                
                results.add(testcaseResult);
            }
            
            // 8. 所有测试用例通过，设置为通过状态
            if (allPassed) {
                log.info("所有测试用例通过: {}", submissionId);
                
                JudgeResult judgeResult = JudgeResult.builder()
                        .status(JudgeConstant.STATUS_ACCEPTED)
                        .executeTime(results.isEmpty() ? 0 : totalTime / results.size())
                        .memoryUsage(maxMemory)
                        .testcaseResults(results)
                        .passedCount(results.size())
                        .totalCount(testCases.size())
                    .build();
                
                saveJudgeResult(submissionId, judgeResult);
            } else {
                log.warn("测试用例未通过: {}", submissionId);
                
                JudgeResult judgeResult = JudgeResult.builder()
                        .status(JudgeConstant.STATUS_WRONG_ANSWER)
                        .executeTime(totalTime / results.size())
                        .memoryUsage(maxMemory)
                        .testcaseResults(results)
                        .passedCount((int) results.stream().filter(TestcaseResult::getPassed).count())
                        .totalCount(testCases.size())
                        .build();
                
                saveJudgeResult(submissionId, judgeResult);
            }
        } catch (Exception e) {
            log.error("评测异常: {}", submissionId, e);
            
            JudgeResult judgeResult = JudgeResult.builder()
                    .status(JudgeConstant.STATUS_SYSTEM_ERROR)
                    .errorMessage("系统错误: " + e.getMessage())
                    .build();
            
            saveJudgeResult(submissionId, judgeResult);
        } finally {
            // 清除当前线程的提交ID
            JudgeContextHolder.clear();
        }
    }

    /**
     * 保存用户代码到本地文件，供AI评判使用
     */
    private void saveUserCode(Long submissionId, String code, String language) {
        try {
            // 读取配置文件中的AI评测工作目录
            String aiWorkspace = env.getProperty("judge.ai-workspace");
            if (aiWorkspace == null || aiWorkspace.isEmpty()) {
                // 如果没有配置，则使用默认目录
                aiWorkspace = System.getProperty("user.dir") + "/ai-judge";
            }
            
            String workspacePath = aiWorkspace + "/" + submissionId;
            File workspace = new File(workspacePath);
            if (!workspace.exists()) {
                workspace.mkdirs();
            }
            
            String fileName = "Solution." + getFileExtension(language);
            File codeFile = new File(workspace, fileName);
            
            java.nio.file.Files.write(
                codeFile.toPath(), 
                code.getBytes(), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
            );
            
            log.info("保存用户代码到: {}", codeFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("保存用户代码失败", e);
        }
    }
    
    /**
     * 根据语言获取文件扩展名
     */
    private String getFileExtension(String language) {
        if (language == null) {
            return "txt";
        }
        
        switch (language.toLowerCase()) {
            case "java":
                return "java";
            case "python":
                return "py";
            case "cpp":
            case "c++":
                return "cpp";
            case "c":
                return "c";
            case "javascript":
            case "js":
                return "js";
            case "csharp":
            case "c#":
                return "cs";
            default:
                return "txt";
        }
    }

    @Override
    public void saveJudgeResult(Long submissionId, JudgeResult result) {
        try {
            log.info("保存评测结果: {}, 状态: {}", submissionId, result.getStatus());
            log.info("评测结果详情 - 执行时间: {}, 内存使用: {}", result.getExecuteTime(), result.getMemoryUsage());
            
            // 获取完整提交记录，以便访问userId和problemId
            Submission existingSubmission = submissionMapper.selectById(submissionId);
            if (existingSubmission == null) {
                log.error("提交记录不存在: {}", submissionId);
                return;
            }
            
            Long userId = existingSubmission.getUserId();
            Long problemId = existingSubmission.getProblemId();
            log.info("提交用户ID: {}, 题目ID: {}", userId, problemId);
            
            // 更新提交状态
            Submission submission = new Submission();
            submission.setId(submissionId);
            submission.setStatus(result.getStatus());
            int submissionUpdateResult = submissionMapper.updateById(submission);
            log.info("更新submission表结果: {} 行受影响", submissionUpdateResult);
            
            // 更新程序提交详情
            ProgramSubmission programSubmission = new ProgramSubmission();
            programSubmission.setSubmissionId(submissionId);
            programSubmission.setErrorMessage(result.getErrorMessage());
            
            // 添加执行时间和内存使用信息
            if (result.getExecuteTime() != null) {
                programSubmission.setExecuteTime(result.getExecuteTime().intValue());
                log.info("设置执行时间: {} ms", result.getExecuteTime().intValue());
            }
            if (result.getMemoryUsage() != null) {
                // 内存使用从KB转为MB
                programSubmission.setMemoryUsage((int)(result.getMemoryUsage() / 1024));
                log.info("设置内存使用: {} MB (原始值: {} KB)", (int)(result.getMemoryUsage() / 1024), result.getMemoryUsage());
            }
            
            // 序列化测试用例结果为JSON
            if (result.getTestcaseResults() != null && !result.getTestcaseResults().isEmpty()) {
                String testcaseResultsJson = objectMapper.writeValueAsString(result.getTestcaseResults());
                programSubmission.setTestcaseResults(testcaseResultsJson);
            }
            
            int programUpdateResult = programSubmissionMapper.updateById(programSubmission);
            log.info("更新program_submission表结果: {} 行受影响", programUpdateResult);
            
            // 定义用户题目状态
            String userProblemStatus = JudgeConstant.STATUS_ACCEPTED.equals(result.getStatus()) 
                ? SubmissionConstant.USER_STATUS_SOLVED 
                : SubmissionConstant.USER_STATUS_ATTEMPTED;
                
            log.info("设置用户题目状态为: {}", userProblemStatus);
            
            // 使用强制更新状态接口代替普通更新接口
            try {
                // 使用userProblemStatusService进行强制更新状态
                boolean statusUpdateResult = userProblemStatusService.forceUpdateStatus(
                    userId, problemId, userProblemStatus);
                
                if (statusUpdateResult) {
                    log.info("用户题目状态更新成功");
                } else {
                    log.warn("用户题目状态强制更新失败，尝试使用内部方法更新");
                    // 如果强制更新失败，尝试使用内部方法
                    updateUserProblemStatus(userId, problemId, userProblemStatus);
                }
            } catch (Exception e) {
                log.error("调用forceUpdateStatus方法失败，尝试使用内部方法", e);
                // 如果调用服务接口失败，尝试使用内部方法
                updateUserProblemStatus(userId, problemId, userProblemStatus);
            }
            
            // 如果评测结果不是ACCEPTED，则添加到错题本
            if (!JudgeConstant.STATUS_ACCEPTED.equals(result.getStatus())) {
                log.info("题目评测未通过，添加到错题本: userId={}, problemId={}", userId, problemId);
                try {
                    errorProblemService.addErrorProblem(userId, problemId);
                    log.info("错题本更新成功");
                } catch (Exception e) {
                    log.error("添加到错题本失败", e);
                }
            }
            
            log.info("评测结果保存完成: {}", submissionId);
        } catch (Exception e) {
            log.error("保存评测结果失败: {}", submissionId, e);
        }
    }
    
    /**
     * 比较输出结果（忽略空白符差异和格式差异）
     */
    private boolean compareOutput(String actual, String expected) {
        if (actual == null || expected == null) {
            return actual == expected;
        }
        
        try {
        // 统一行尾，清理多余空白
        actual = normalizeOutput(actual);
        expected = normalizeOutput(expected);
        
            // 如果完全相等，直接返回true
            if (actual.equals(expected)) {
                return true;
            }
            
            // 处理数组格式差异，比如 "[0, 1]" 和 "0 1"
            // 先尝试移除数组表示法中的符号
            String normalizedActual = actual.replaceAll("[\\[\\]\\{\\}]", "").replaceAll(",\\s*", " ").trim();
            String normalizedExpected = expected.replaceAll("[\\[\\]\\{\\}]", "").replaceAll(",\\s*", " ").trim();
            
            // 再次比较
            if (normalizedActual.equals(normalizedExpected)) {
                return true;
            }
            
            // 更进一步，将两个字符串转为数字数组进行比较
            // 这样能处理空格数量或分隔符的差异
            String[] actualParts = normalizedActual.split("\\s+");
            String[] expectedParts = normalizedExpected.split("\\s+");
            
            if (actualParts.length != expectedParts.length) {
                return false;
            }
            
            // 比较每个数字
            for (int i = 0; i < actualParts.length; i++) {
                try {
                    double actualNum = Double.parseDouble(actualParts[i]);
                    double expectedNum = Double.parseDouble(expectedParts[i]);
                    if (Math.abs(actualNum - expectedNum) > 1e-9) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // 如果无法解析为数字，则比较原始字符串
                    if (!actualParts[i].equals(expectedParts[i])) {
                        return false;
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            log.warn("比较输出时发生异常: {}", e.getMessage());
            // 如果发生异常，回退到原始比较
        return actual.equals(expected);
        }
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
     * 从题目中获取测试用例
     * @param problem 编程题目
     * @return 测试用例列表
     */
    private List<TestCaseDTO> getTestCases(ProgramProblem problem) {
        try {
            // 检查题目是否包含测试用例数据
            String testCasesJson = problem.getTestCases();
            if (testCasesJson == null || testCasesJson.isEmpty()) {
                log.warn("题目没有测试用例: {}", problem.getId());
                return new ArrayList<>();
            }
            
            // 将JSON解析为测试用例对象列表
            return Arrays.asList(objectMapper.readValue(testCasesJson, TestCaseDTO[].class));
        } catch (Exception e) {
            log.error("解析测试用例失败: {}", problem.getId(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新用户题目状态，实现"只升不降"的逻辑，增加重试机制
     * 状态优先级: SOLVED > ATTEMPTED > UNSOLVED
     */
    private void updateUserProblemStatus(Long userId, Long problemId, String status) {
        try {
            log.info("开始更新用户题目状态(内部方法): userId={}, problemId={}, status={}", userId, problemId, status);
            
            // 确定正确的用户题目状态（将评测结果状态转换为用户题目状态）
            String userStatus;
            if (JudgeConstant.STATUS_ACCEPTED.equals(status)) {
                userStatus = SubmissionConstant.USER_STATUS_SOLVED;
            } else if (status.equals(SubmissionConstant.USER_STATUS_SOLVED) || 
                       status.equals(SubmissionConstant.USER_STATUS_ATTEMPTED)) {
                userStatus = status; // 已经是用户题目状态，直接使用
            } else {
                userStatus = SubmissionConstant.USER_STATUS_ATTEMPTED;
            }
            
            log.info("转换后的用户题目状态: {}", userStatus);
            
            // 增加重试机制
            int retryCount = 0;
            boolean updated = false;
            Exception lastException = null;
            
            while (!updated && retryCount < 3) {
                try {
                    // 先尝试使用服务层方法强制更新
                    try {
                        boolean forcedResult = userProblemStatusService.forceUpdateStatus(userId, problemId, userStatus);
                        if (forcedResult) {
                            log.info("通过Service强制更新状态成功");
                            return;
                        } else {
                            log.warn("通过Service强制更新状态失败，尝试直接操作数据库");
                        }
                    } catch (Exception e) {
                        log.warn("调用强制更新方法失败，回退到直接数据库操作: {}", e.getMessage());
                    }
                    
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
                        userProblemStatus.setStatus(userStatus);
                        userProblemStatus.setLastSubmitTime(LocalDateTime.now());
                        userProblemStatusMapper.insert(userProblemStatus);
                        updated = true;
                        log.info("用户题目状态创建成功");
                        return;
                    } else {
                        log.info("找到现有状态: {}", userProblemStatus);
                        String currentStatus = userProblemStatus.getStatus();
                        
                        // 如果当前状态是已解决，则不再更新
                        if (SubmissionConstant.USER_STATUS_SOLVED.equals(currentStatus)) {
                            log.info("题目已经是SOLVED状态，保持不变");
                            shouldUpdate = false;
                            updated = true; // 无需更新也视为成功
                        } 
                        // 如果要更新为ATTEMPTED，但当前已经是更高级别的状态，则不更新
                        else if (SubmissionConstant.USER_STATUS_ATTEMPTED.equals(userStatus) && 
                                SubmissionConstant.USER_STATUS_SOLVED.equals(currentStatus)) {
                            log.info("当前状态为SOLVED，新状态为ATTEMPTED，保持不变");
                            shouldUpdate = false;
                            updated = true; // 无需更新也视为成功
                        }
                        // 其他情况：从UNSOLVED升级到任何状态，或从ATTEMPTED升级到SOLVED，都更新
                    }
                    
                    if (shouldUpdate) {
                        log.info("更新用户题目状态: {} -> {}", userProblemStatus.getStatus(), userStatus);
                        userProblemStatus.setStatus(userStatus);
                        userProblemStatus.setLastSubmitTime(LocalDateTime.now());
                        int result = userProblemStatusMapper.updateById(userProblemStatus);
                        updated = result > 0;
                        log.info("用户题目状态更新结果: {}", updated ? "成功" : "失败");
                    }
                } catch (Exception e) {
                    retryCount++;
                    lastException = e;
                    log.warn("用户题目状态更新失败(尝试 {}/3): {}", retryCount, e.getMessage());
                    try {
                        // 重试前等待短暂时间
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            if (!updated) {
                log.error("用户题目状态更新失败，已达最大重试次数: userId={}, problemId={}", userId, problemId);
                if (lastException != null) {
                    log.error("最后一次异常:", lastException);
                }
            }
        } catch (Exception e) {
            log.error("更新用户题目状态异常", e);
        }
    }
} 