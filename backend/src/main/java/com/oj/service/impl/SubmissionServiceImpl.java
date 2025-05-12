package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.exception.BusinessException;
import com.oj.mapper.*;
import com.oj.model.entity.*;
import com.oj.model.request.ChoiceJudgeSubmissionRequest;
import com.oj.model.request.ProgramSubmissionRequest;
import com.oj.model.vo.*;
import com.oj.service.ErrorProblemService;
import com.oj.service.JudgeService;
import com.oj.service.SubmissionService;
import com.oj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oj.constant.SubmissionConstant.*;

@Service
@Slf4j
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {

    @Resource
    private ChoiceProblemMapper choiceProblemMapper;
    
    @Resource
    private JudgeProblemMapper judgeProblemMapper;
    
    @Resource
    private ProblemMapper problemMapper;
    
    @Resource
    private ChoiceJudgeSubmissionMapper choiceJudgeSubmissionMapper;
    
    @Resource
    private ProgramSubmissionMapper programSubmissionMapper;
    
    @Resource
    private UserProblemStatusMapper userProblemStatusMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private programProblemMapper programProblemMapper;

    @Resource
  private   JudgeService judgeService;

    @Resource
    private ErrorProblemService errorProblemService;

    @Override
    @Transactional
    public Long submitChoiceJudgeAnswer(Long userId, ChoiceJudgeSubmissionRequest request) {
        // 检查题目是否存在
        Problem problem = problemMapper.selectById(request.getProblemId());
        if (problem == null) {
            throw new BusinessException(40000, "题目不存在");
        }
        
        // 创建基础提交记录
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(request.getProblemId());
        submission.setType(request.getType());
        submission.setSubmissionTime(LocalDateTime.now());
        
        // 获取正确答案并比对
        String correctAnswer = "";
        if (TYPE_CHOICE.equals(request.getType())) {
            ChoiceProblem choiceProblem = choiceProblemMapper.selectById(request.getProblemId());
            if (choiceProblem == null) {
                throw new BusinessException(40000, "选择题不存在");
            }
            correctAnswer = choiceProblem.getAnswer();
        } else if (TYPE_JUDGE.equals(request.getType())) {
            JudgeProblem judgeProblem = judgeProblemMapper.selectById(request.getProblemId());
            if (judgeProblem == null) {
                throw new BusinessException(40000, "判断题不存在");
            }
            // 将Boolean转为String
            correctAnswer = String.valueOf(judgeProblem.getAnswer());
        } else {
            throw new BusinessException(40000, "题目类型错误");
        }
        
        // 判断回答是否正确
        boolean isCorrect = correctAnswer.equals(request.getAnswer());
        submission.setStatus(isCorrect ? STATUS_CORRECT : STATUS_WRONG);
        
        // 保存提交记录
        log.info("保存提交记录: {}", submission);
        this.save(submission);
        log.info("提交记录已保存，ID: {}", submission.getId());
        
        // 保存选择/判断题提交详情
        ChoiceJudgeSubmission choiceJudgeSubmission = new ChoiceJudgeSubmission();
        choiceJudgeSubmission.setSubmissionId(submission.getId());
        choiceJudgeSubmission.setAnswer(request.getAnswer());
        log.info("保存提交详情: {}", choiceJudgeSubmission);
        choiceJudgeSubmissionMapper.insert(choiceJudgeSubmission);
        log.info("提交详情已保存");
        
        // 更新用户题目状态
        log.info("开始更新用户题目状态: userId={}, problemId={}, 提交结果={}", 
            userId, request.getProblemId(), isCorrect);
        
        // 获取当前用户题目状态
        UserProblemStatus existingStatus = userProblemStatusMapper.selectByUserAndProblem(userId, request.getProblemId());
        
        String newStatus = isCorrect ? USER_STATUS_SOLVED : USER_STATUS_ATTEMPTED;
        
        // 检查是否需要更新状态
        boolean shouldUpdate = true;
        
        if (existingStatus != null) {
            log.info("当前题目状态: {}", existingStatus.getStatus());
            
            // 如果当前状态是SOLVED，无论新状态如何都不更新
            if (USER_STATUS_SOLVED.equals(existingStatus.getStatus())) {
                log.info("题目已经是SOLVED状态，保持不变");
                shouldUpdate = false;
            }
            // 如果当前状态是ATTEMPTED，且新状态不是SOLVED，则不更新
            else if (USER_STATUS_ATTEMPTED.equals(existingStatus.getStatus()) && !USER_STATUS_SOLVED.equals(newStatus)) {
                log.info("当前状态为ATTEMPTED，新状态不是SOLVED，保持不变");
                shouldUpdate = false;
            }
        }
        
        if (shouldUpdate) {
            try {
                log.info("更新用户题目状态: {} -> {}", 
                    existingStatus != null ? existingStatus.getStatus() : "NULL", newStatus);
                int result = userProblemStatusMapper.upsertStatus(userId, request.getProblemId(), newStatus);
                log.info("更新用户题目状态结果: {}", result > 0 ? "成功" : "失败");
            } catch (Exception e) {
                log.error("更新用户题目状态异常", e);
            }
        } else {
            log.info("无需更新用户题目状态");
        }
        
        // 如果回答错误，添加到错题本
        if (!isCorrect) {
            log.info("题目回答错误，添加到错题本: userId={}, problemId={}", userId, request.getProblemId());
            errorProblemService.addErrorProblem(userId, request.getProblemId());
        }
        
        return submission.getId();
    }

    @Override
    @Transactional
    public Long submitProgramCode(Long userId, ProgramSubmissionRequest request) {
        // 检查题目是否存在
        Problem problem = problemMapper.selectById(request.getProblemId());
        if (problem == null) {
            throw new BusinessException(40000, "题目不存在");
        }
        
        // 检查编程题是否存在
        ProgramProblem programProblem = programProblemMapper.selectById(request.getProblemId());
        if (programProblem == null) {
            throw new BusinessException(40000, "编程题不存在");
        }
        
        // 创建基础提交记录
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(request.getProblemId());
        submission.setType(TYPE_PROGRAM);
        submission.setSubmissionTime(LocalDateTime.now());
        
        // 保存提交记录，状态默认为PENDING
        submission.setStatus(STATUS_PENDING);
        this.save(submission);
        
        // 保存编程题提交详情
        ProgramSubmission programSubmission = new ProgramSubmission();
        programSubmission.setSubmissionId(submission.getId());
        programSubmission.setLanguage(request.getLanguage());
        programSubmission.setCode(request.getCode());
        programSubmissionMapper.insert(programSubmission);
        
        // 在事务提交后执行评测任务（异步）
        Long submissionId = submission.getId();
        TransactionSynchronizationManager.registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    judgeService.submitJudgeTask(submissionId);
                }
            }
        );
        
        return submission.getId();
    }

    @Override
    public Page<SubmissionListVO> getUserSubmissions(Long userId, long current, long size,
                                                  String type, String status, String difficulty,
                                                  String jobType, String tag, String keyword) {
        // 1. 先获取符合条件的题目ID列表
        List<Long> filteredProblemIds = getFilteredProblemIds(difficulty, jobType, tag, keyword);
        
        // 2. 构建查询条件
        LambdaQueryWrapper<Submission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Submission::getUserId, userId)
                   .eq(Submission::getIsDelete, 0)
                   .in(Submission::getProblemId, filteredProblemIds);  // 使用IN查询
        
        // 3. 添加其他筛选条件
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(Submission::getType, type);
        }
        
        if (status != null && !status.isEmpty()) {
            if ("ACCEPTED".equals(status)) {
                queryWrapper.and(w -> w.eq(Submission::getStatus, STATUS_ACCEPTED)
                                      .or()
                                      .eq(Submission::getStatus, STATUS_CORRECT));
            } else {
                queryWrapper.eq(Submission::getStatus, status);
            }
        }
        
        // 4. 执行分页查询
        Page<Submission> page = new Page<>(current, size);
        queryWrapper.orderByDesc(Submission::getSubmissionTime);
        Page<Submission> submissionPage = this.page(page, queryWrapper);
        
        // 5. 转换为VO并返回
        return convertToVO(submissionPage);
    }

    // 获取符合条件的题目ID列表
    private List<Long> getFilteredProblemIds(String difficulty, String jobType, String tag, String keyword) {
        LambdaQueryWrapper<Problem> problemWrapper = new LambdaQueryWrapper<>();
        
        if (difficulty != null && !difficulty.isEmpty()) {
            problemWrapper.eq(Problem::getDifficulty, difficulty);
        }
        
        if (jobType != null && !jobType.isEmpty()) {
            problemWrapper.eq(Problem::getJobType, jobType);
        }
        
        if (tag != null && !tag.isEmpty()) {
            problemWrapper.like(Problem::getTags, tag);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            problemWrapper.like(Problem::getTitle, keyword)
                         .or()
                         .like(Problem::getContent, keyword)
                         .or()
                         .like(Problem::getTags, keyword);
        }
        
        return problemMapper.selectList(problemWrapper)
                           .stream()
                           .map(Problem::getId)
                           .collect(Collectors.toList());
    }

    @Override
    public Page<SubmissionListVO> getProblemSubmissions(Long problemId, Long userId, long current, long size) {
        if (userId == null) {
            throw new BusinessException(40000, "用户ID不能为空");
        }
        
        // 查询题目的提交记录
        Page<Submission> page = new Page<>(current, size);
        LambdaQueryWrapper<Submission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Submission::getProblemId, problemId)
                   .eq(Submission::getUserId, userId)  // 必须过滤用户ID
                   .eq(Submission::getIsDelete, 0)
                   .orderByDesc(Submission::getSubmissionTime);
        
        Page<Submission> submissionPage = this.page(page, queryWrapper);
        
        // 转换为VO
        Page<SubmissionListVO> resultPage = new Page<>(
                submissionPage.getCurrent(),
                submissionPage.getSize(),
                submissionPage.getTotal());
        
        if (submissionPage.getRecords().isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }
        
        // 查询题目信息
        Problem problem = problemMapper.selectById(problemId);
        
        // 构建VO列表
        List<SubmissionListVO> submissionVOList = submissionPage.getRecords().stream()
                .map(submission -> {
                    SubmissionListVO vo;
                    
                    // 根据提交类型创建不同的VO
                    if (TYPE_PROGRAM.equals(submission.getType())) {
                        ProgramSubmissionListVO programVO = new ProgramSubmissionListVO();
                        // 查询编程题提交详情
                        ProgramSubmission programSubmission = programSubmissionMapper.selectById(submission.getId());
                        if (programSubmission != null) {
                            programVO.setLanguage(programSubmission.getLanguage());
                            programVO.setExecuteTime(programSubmission.getExecuteTime());
                            programVO.setMemoryUsage(programSubmission.getMemoryUsage());
                        }
                        vo = programVO;
                    } else {
                        ChoiceJudgeSubmissionListVO choiceJudgeVO = new ChoiceJudgeSubmissionListVO();
                        // 查询选择/判断题提交详情
                        ChoiceJudgeSubmission choiceJudgeSubmission = choiceJudgeSubmissionMapper.selectById(submission.getId());
                        if (choiceJudgeSubmission != null) {
                            choiceJudgeVO.setAnswer(choiceJudgeSubmission.getAnswer());
                        }
                        vo = choiceJudgeVO;
                    }
                    
                    // 设置基本属性
                    vo.setId(submission.getId());
                    vo.setProblemId(submission.getProblemId());
                    vo.setType(submission.getType());
                    vo.setStatus(submission.getStatus());
                    vo.setSubmissionTime(submission.getSubmissionTime());
                    
                    // 设置题目相关信息
                    if (problem != null) {
                        vo.setProblemTitle(problem.getTitle());
                        vo.setDifficulty(problem.getDifficulty());
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        resultPage.setRecords(submissionVOList);
        return resultPage;
    }

    @Override
    public SubmissionDetailVO getSubmissionDetail(Long submissionId, Long userId) {
        // 根据提交类型选择具体实现方法
        Submission submission = this.getById(submissionId);
        if (submission == null) {
            throw new BusinessException(40000, "提交记录不存在");
        }
        
        // 判断提交类型并调用相应的方法
        if (TYPE_CHOICE.equals(submission.getType()) || 
            TYPE_JUDGE.equals(submission.getType())) {
            return getChoiceJudgeSubmissionDetail(submissionId, userId);
        } else if (TYPE_PROGRAM.equals(submission.getType())) {
            return getProgramSubmissionDetail(submissionId, userId);
        } else {
            throw new BusinessException(40000, "未知的提交类型");
        }
    }

    @Override
    public ChoiceJudgeSubmissionDetailVO getChoiceJudgeSubmissionDetail(Long submissionId, Long userId) {
        // 查询提交记录
        Submission submission = this.getById(submissionId);
        if (submission == null) {
            throw new BusinessException(40000, "提交记录不存在");
        }
        
        // 检查权限
        boolean canViewDetail = submission.getUserId().equals(userId);
        
        // 创建详情VO
        ChoiceJudgeSubmissionDetailVO detailVO = new ChoiceJudgeSubmissionDetailVO();
        
        // 设置基本信息
        BeanUtils.copyProperties(submission, detailVO);
        detailVO.setId(submission.getId());
        detailVO.setSubmissionTime(submission.getSubmissionTime());
        detailVO.setStatus(submission.getStatus());
        
        // 查询题目信息
        Problem problem = problemMapper.selectById(submission.getProblemId());
        if (problem != null) {
            detailVO.setProblemTitle(problem.getTitle());
            detailVO.setProblemContent(problem.getContent());
            // 解析tags字符串为List
            if (problem.getTags() != null && !problem.getTags().isEmpty()) {
                detailVO.setProblemTags(Arrays.asList(problem.getTags().split(",")));
            } else {
                detailVO.setProblemTags(new ArrayList<>());
            }
            detailVO.setJobType(problem.getJobType());
            detailVO.setDifficulty(problem.getDifficulty());
        } else {
            // 如果题目已被删除，设置默认信息
            detailVO.setProblemTitle("已删除的题目");
            detailVO.setProblemContent("该题目已被删除");
            detailVO.setProblemTags(new ArrayList<>());
            detailVO.setJobType("未知");
            detailVO.setDifficulty("未知");
        }
        
        // 查询用户信息
        // TODO: 添加用户服务，获取用户名
        detailVO.setUserName("用户" + submission.getUserId());
        
        // 获取选择/判断题详情
        ChoiceJudgeSubmission choiceJudgeSubmission = choiceJudgeSubmissionMapper.selectById(submissionId);
        if (choiceJudgeSubmission != null) {
            // 用户提交的答案，无需权限校验
            detailVO.setAnswer(choiceJudgeSubmission.getAnswer());
            
            // 查询正确答案和解析
            if (TYPE_CHOICE.equals(submission.getType())) {
                ChoiceProblem choiceProblem = choiceProblemMapper.selectById(submission.getProblemId());
                if (choiceProblem != null) {
                    // 设置选项 - 实际代码中需要解析JSON字符串
                    // 这里简单处理，实际项目中需要更完善的实现
                    // detailVO.setOptions(parseOptionsFromJson(choiceProblem.getOptions()));
                    
                    // 根据权限或条件显示答案和解析
                    detailVO.setCanViewAnalysis(canViewDetail || STATUS_CORRECT.equals(submission.getStatus()));
                    
                    if (Boolean.TRUE.equals(detailVO.getCanViewAnalysis())) {
                        detailVO.setCorrectAnswer(choiceProblem.getAnswer());
                        detailVO.setAnalysis(choiceProblem.getAnalysis());
                    }
                }
            } else if (TYPE_JUDGE.equals(submission.getType())) {
                JudgeProblem judgeProblem = judgeProblemMapper.selectById(submission.getProblemId());
                if (judgeProblem != null) {
                    // 根据权限或条件显示答案和解析
                    detailVO.setCanViewAnalysis(canViewDetail || STATUS_CORRECT.equals(submission.getStatus()));
                    
                    if (Boolean.TRUE.equals(detailVO.getCanViewAnalysis())) {
                        // 将Boolean转为String
                        detailVO.setCorrectAnswer(String.valueOf(judgeProblem.getAnswer()));
                        detailVO.setAnalysis(judgeProblem.getAnalysis());
                    }
                }
            }
        }
        
        return detailVO;
    }

    @Override
    public ProgramSubmissionDetailVO getProgramSubmissionDetail(Long submissionId, Long userId) {
        // 查询提交记录
        Submission submission = this.getById(submissionId);
        if (submission == null) {
            throw new BusinessException(40000, "提交记录不存在");
        }
        
        // 检查权限
        boolean isOwner = submission.getUserId().equals(userId);
        if (!isOwner) {
            // 非本人只能查看通过的提交
            if (!STATUS_ACCEPTED.equals(submission.getStatus())) {
                throw new BusinessException(40300, "无权查看该提交详情");
            }
        }
        
        // 创建详情VO
        ProgramSubmissionDetailVO detailVO = new ProgramSubmissionDetailVO();
        
        // 设置基本信息
        BeanUtils.copyProperties(submission, detailVO);
        detailVO.setId(submission.getId());
        detailVO.setSubmissionTime(submission.getSubmissionTime());
        detailVO.setStatus(submission.getStatus());
        
        // 查询题目信息
        Problem problem = problemMapper.selectById(submission.getProblemId());
        if (problem != null) {
            detailVO.setProblemTitle(problem.getTitle());
            detailVO.setProblemContent(problem.getContent());
            // 解析tags字符串为List
            if (problem.getTags() != null && !problem.getTags().isEmpty()) {
                detailVO.setProblemTags(Arrays.asList(problem.getTags().split(",")));
            } else {
                detailVO.setProblemTags(new ArrayList<>());
            }
            detailVO.setJobType(problem.getJobType());
            detailVO.setDifficulty(problem.getDifficulty());
        } else {
            // 如果题目已被删除，设置默认信息
            detailVO.setProblemTitle("已删除的题目");
            detailVO.setProblemContent("该题目已被删除");
            detailVO.setProblemTags(new ArrayList<>());
            detailVO.setJobType("未知");
            detailVO.setDifficulty("未知");
        }
        
        // 查询用户信息
        // TODO: 添加用户服务，获取用户名
        detailVO.setUserName("用户" + submission.getUserId());
        
        // 查询编程题详情
        ProgramProblem programProblem = programProblemMapper.selectById(submission.getProblemId());
        if (programProblem != null) {
            detailVO.setFunctionName(programProblem.getFunctionName());
            // 解析参数类型字符串为List
            if (programProblem.getParamTypes() != null && !programProblem.getParamTypes().isEmpty()) {
                detailVO.setParamTypes(Arrays.asList(programProblem.getParamTypes().split(",")));
            } else {
                detailVO.setParamTypes(new ArrayList<>());
            }
            detailVO.setReturnType(programProblem.getReturnType());
            // 类型转换：将Long转为Integer
            detailVO.setTimeLimit(programProblem.getTimeLimit() != null ? 
                programProblem.getTimeLimit().intValue() : null);
            detailVO.setMemoryLimit(programProblem.getMemoryLimit() != null ? 
                programProblem.getMemoryLimit().intValue() : null);
        }
        
        // 查询提交详情
        ProgramSubmission programSubmission = programSubmissionMapper.selectById(submissionId);
        if (programSubmission != null) {
            detailVO.setLanguage(programSubmission.getLanguage());
            detailVO.setCode(programSubmission.getCode());
            detailVO.setExecuteTime(programSubmission.getExecuteTime());
            detailVO.setMemoryUsage(programSubmission.getMemoryUsage());
            detailVO.setTestcaseResults(programSubmission.getTestcaseResults());
            detailVO.setErrorMessage(programSubmission.getErrorMessage());
            
            // 计算通过率
            // if (programSubmission.getTestcaseResults() != null) {
            //     try {
            //         // 简单处理：计算通过的测试用例数
            //         String[] results = programSubmission.getTestcaseResults().split("\n");
            //         int passed = 0;
            //         for (String result : results) {
            //             if (result.trim().startsWith("通过") || result.trim().contains("PASS")) {
            //                 passed++;
            //             }
            //         }
            //         detailVO.setPassedTestCases(passed);
            //         detailVO.setTotalTestCases(results.length);
            //     } catch (Exception e) {
            //         // 解析失败则不设置值
            //         log.error("解析测试用例结果失败: {}", e.getMessage());
            //     }
            // }
            try {
                // 解析JSON测试结果
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> testcases = mapper.readValue(
                    programSubmission.getTestcaseResults(),
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                
                int passed = 0;
                for (Map<String, Object> testcase : testcases) {
                    if (Boolean.TRUE.equals(testcase.get("passed"))) {
                        passed++;
                    }
                }
                
                detailVO.setPassedTestCases(passed);
                detailVO.setTotalTestCases(testcases.size());
            } catch (Exception e) {
                log.error("解析测试用例结果失败: {}", e.getMessage());
            }
        }
        
        // 添加标准答案 - 只有管理员或题目创建者可见
        if (programProblem != null && programProblem.getStandardSolution() != null && 
                !programProblem.getStandardSolution().isEmpty()) {
            // 判断当前用户是否为管理员或题目创建者
            boolean isAdmin = userService.isAdmin(userId);
            boolean isCreator = problem != null && userId.equals(problem.getUserId());
            
            if (isAdmin || isCreator) {
                try {
                    // 将标准答案的JSON字符串转换为Map
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> standardSolution = objectMapper.readValue(
                            programProblem.getStandardSolution(),
                            new TypeReference<Map<String, String>>() {}
                    );
                    detailVO.setStandardSolution(standardSolution);
                } catch (Exception e) {
                    log.error("解析编程题标准答案失败: {}", e.getMessage());
                }
            }
        }
        
        return detailVO;
    }

    @Override
    @Transactional
    public void updateUserProblemStatus(Long userId, Long problemId, String status) {
        try {
            log.info("开始更新用户题目状态: userId={}, problemId={}, status={}", userId, problemId, status);

            // 先检查是否存在记录及其当前状态
            UserProblemStatus existingStatus = userProblemStatusMapper.selectByUserAndProblem(userId, problemId);
            
            // 检查更新条件
            boolean shouldUpdate = true;
            
            if (existingStatus == null) {
                // 没有记录，直接创建新记录
                log.info("用户题目状态不存在，创建新记录，状态: {}", status);
                int result = userProblemStatusMapper.upsertStatus(userId, problemId, status);
                log.info("创建用户题目状态结果: {}", result > 0 ? "成功" : "失败");
                return;
            }
            
            log.info("找到现有状态: {}", existingStatus);
            String currentStatus = existingStatus.getStatus();
            
            // 状态优先级: SOLVED > ATTEMPTED > UNSOLVED
            // 只有新状态优先级更高时才更新
            if (USER_STATUS_SOLVED.equals(currentStatus)) {
                // 当前已是最高状态"已解决"，不再更新
                log.info("题目已经是SOLVED状态，保持不变");
                shouldUpdate = false;
            } 
            else if (USER_STATUS_ATTEMPTED.equals(status) && USER_STATUS_SOLVED.equals(currentStatus)) {
                // 如果要降级：从已解决降为已尝试，不允许
                log.info("当前状态为SOLVED，新状态为ATTEMPTED，不更新");
                shouldUpdate = false;
            }
            else if (USER_STATUS_UNSOLVED.equals(status)) {
                // 如果要降级为未解决，不允许
                log.info("新状态为UNSOLVED，不允许降级，保持状态: {}", currentStatus);
                shouldUpdate = false;
            }
            
            if (shouldUpdate) {
                // 使用一条SQL完成插入或更新
                log.info("更新用户题目状态: {} -> {}", currentStatus, status);
                int result = userProblemStatusMapper.upsertStatus(userId, problemId, status);
                log.info("更新用户题目状态结果: {}", result > 0 ? "成功" : "失败");
            } else {
                log.info("无需更新用户题目状态");
            }
        } catch (Exception e) {
            log.error("更新用户题目状态异常", e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }

    @Override
    public Long countProblemSubmissions(Long problemId) {
        LambdaQueryWrapper<Submission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Submission::getProblemId, problemId)
                   .eq(Submission::getIsDelete, 0);
        return this.count(queryWrapper);
    }

    @Override
    public Long countUserSubmissions(Long userId) {
        // 使用LambdaQueryWrapper查询
        LambdaQueryWrapper<Submission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Submission::getUserId, userId)
                   .eq(Submission::getIsDelete, 0);
        return this.count(queryWrapper);
    }

    @Override
    public Page<SubmissionListVO> getRecentSubmissionsByUserId(long userId, long pageNum, long pageSize) {
        // TODO: 实现获取用户最近提交记录的逻辑
        return null;
    }
    
    @Override
    public Integer countSubmissions() {
        // 查询所有提交数量并转为Integer
        return Math.toIntExact(this.count());
    }

    /**
     * 延迟执行评测任务，确保事务已提交
     * @param submissionId 提交ID
     */
    @Async("judgeTaskExecutor")
    public void delayedJudgeTask(Long submissionId) {
        try {
            // 等待一段时间，确保事务已提交
            Thread.sleep(1000);
            // 执行评测任务
            judgeService.submitJudgeTask(submissionId);
        } catch (InterruptedException e) {
            log.error("延迟评测任务被中断", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("延迟评测任务异常: {}", submissionId, e);
        }
    }

    private Page<SubmissionListVO> convertToVO(Page<Submission> submissionPage) {
        // 转换为VO
        Page<SubmissionListVO> resultPage = new Page<>(
                submissionPage.getCurrent(),
                submissionPage.getSize(),
                submissionPage.getTotal());
        
        if (submissionPage.getRecords().isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }
        
        // 查询题目信息，填充题目标题和难度
        List<Long> problemIds = submissionPage.getRecords().stream()
                .map(Submission::getProblemId)
                .collect(Collectors.toList());
        
        List<Problem> problems = problemMapper.selectBatchIds(problemIds);
        
        // 将题目信息映射为ID-Problem的形式，方便查找
        Map<Long, Problem> problemMap = problems.stream()
                .collect(Collectors.toMap(Problem::getId, problem -> problem));
        
        // 构建VO列表
        List<SubmissionListVO> submissionVOList = submissionPage.getRecords().stream()
                .map(submission -> {
                    SubmissionListVO vo;
                    
                    // 根据提交类型创建不同的VO
                    if (TYPE_PROGRAM.equals(submission.getType())) {
                        ProgramSubmissionListVO programVO = new ProgramSubmissionListVO();
                        // 查询编程题提交详情
                        ProgramSubmission programSubmission = programSubmissionMapper.selectById(submission.getId());
                        if (programSubmission != null) {
                            programVO.setLanguage(programSubmission.getLanguage());
                            programVO.setExecuteTime(programSubmission.getExecuteTime());
                            programVO.setMemoryUsage(programSubmission.getMemoryUsage());
                        }
                        vo = programVO;
                    } else {
                        ChoiceJudgeSubmissionListVO choiceJudgeVO = new ChoiceJudgeSubmissionListVO();
                        // 查询选择/判断题提交详情
                        ChoiceJudgeSubmission choiceJudgeSubmission = choiceJudgeSubmissionMapper.selectById(submission.getId());
                        if (choiceJudgeSubmission != null) {
                            choiceJudgeVO.setAnswer(choiceJudgeSubmission.getAnswer());
                        }
                        vo = choiceJudgeVO;
                    }
                    
                    // 设置基本属性
                    vo.setId(submission.getId());
                    vo.setProblemId(submission.getProblemId());
                    vo.setType(submission.getType());
                    vo.setStatus(submission.getStatus());
                    vo.setSubmissionTime(submission.getSubmissionTime());
                    
                    // 设置题目相关信息
                    Problem problem = problemMap.get(submission.getProblemId());
                    if (problem != null) {
                        vo.setProblemTitle(problem.getTitle());
                        vo.setDifficulty(problem.getDifficulty());
                        vo.setJobType(problem.getJobType());
                        // 设置标签
                        vo.setTags(problem.getTags() != null ? Arrays.asList(problem.getTags().split(",")) : new ArrayList<>());
                    } else {
                        // 处理题目已删除的情况
                        vo.setProblemTitle("已删除的题目");
                        vo.setDifficulty("未知");
                        vo.setJobType("未知");
                        vo.setTags(new ArrayList<>());
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        resultPage.setRecords(submissionVOList);
        return resultPage;
    }
} 