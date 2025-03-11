package com.oj.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.common.ErrorCode;
import com.oj.common.UserContext;
import com.oj.constant.ProblemConstant;
import com.oj.exception.BusinessException;
import com.oj.mapper.*;
import com.oj.model.dto.ChoiceProblemDTO;
import com.oj.model.dto.ProblemDTO;
import com.oj.model.dto.ProblemOptionDTO;
import com.oj.model.entity.*;
import com.oj.model.enums.ProblemStatusEnum;
import com.oj.model.enums.ProblemTypeEnum;
import com.oj.model.enums.SubmissionStatusEnum;
import com.oj.model.request.*;
import com.oj.model.vo.ProblemVO;
import com.oj.service.ProblemService;
import com.oj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {

    @Resource
    private ProblemMapper problemMapper;

    @Resource
    private ChoiceProblemMapper choiceProblemMapper;

    @Resource
    private JudgeProblemMapper judgeProblemMapper;

    @Resource
    private programProblemMapper programProblemMapper;

    @Resource
    private UserService userService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private SubmissionMapper submissionMapper;

    @Override
    public ProblemMapper getBaseMapper() {
        return problemMapper;
    }

    @Override
    public Class<Problem> getEntityClass() {
        return Problem.class;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addChoiceProblem(ChoiceProblemAddRequest request, Long userId) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        log.info("开始添加选择题: {}, 用户ID: {}", request.getTitle(), userId);

        // 1. 验证选择题基本信息
        validateChoiceProblemRequest(request);

        // 记录选项信息
        log.info("选项信息: {}", request.getOptions());

        // 2. 创建基础题目记录
        Problem problem = new Problem();
        BeanUtils.copyProperties(request, problem);
        problem.setType(ProblemTypeEnum.CHOICE.name());
        problem.setUserId(userId);
        // 设置初始状态为未解决
        problem.setStatus(ProblemStatusEnum.UNSOLVED.getValue());
        // 设置初始提交次数和通过率
        problem.setSubmissionCount(0);
        problem.setAcceptRate("0%");

        // 处理标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                problem.setTags(objectMapper.writeValueAsString(request.getTags()));
                log.info("标签序列化成功: {}", problem.getTags());
            } catch (Exception e) {
                log.error("标签序列化失败: {}", e.getMessage(), e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "标签格式错误: " + e.getMessage());
            }
        }

        log.info("保存基础题目信息");
        boolean success = this.save(problem);
        if (!success || problem.getId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存题目失败");
        }
        log.info("基础题目保存成功, ID: {}", problem.getId());

        // 3. 创建选择题记录
        ChoiceProblem choiceProblem = new ChoiceProblem();
        choiceProblem.setId(problem.getId());
        choiceProblem.setAnswer(request.getAnswer());
        choiceProblem.setAnalysis(request.getAnalysis());

        // 关键修改：将选项列表转换为JSON字符串存储
        try {
            // 确保选项不为空
            if (request.getOptions() == null || request.getOptions().isEmpty()) {
                log.warn("选项列表为空，设置为空数组");
                choiceProblem.setOptions("[]");
            } else {
                log.info("序列化选项列表, 选项数量: {}", request.getOptions().size());
                String optionsJson = objectMapper.writeValueAsString(request.getOptions());
                log.info("选项序列化结果: {}", optionsJson);
                choiceProblem.setOptions(optionsJson);
            }
        } catch (Exception e) {
            log.error("选项序列化失败: {}", e.getMessage(), e);
            this.removeById(problem.getId()); // 回滚基础题目
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "选项格式错误: " + e.getMessage());
        }

        log.info("保存选择题信息, ID: {}", choiceProblem.getId());
        success = choiceProblemMapper.insert(choiceProblem) > 0;
        if (!success) {
            log.error("选择题保存失败, ID: {}", choiceProblem.getId());
            this.removeById(problem.getId()); // 回滚基础题目
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存选择题失败");
        }
        log.info("选择题添加成功, ID: {}", problem.getId());

        return problem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addJudgeProblem(JudgeProblemAddRequest judgeAddRequest, Long userId) {
        if (judgeAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 1. 创建基础题目信息
        Problem problem = new Problem();
        BeanUtils.copyProperties(judgeAddRequest, problem);
        problem.setUserId(userId);
        problem.setType("JUDGE");
        problem.setAcceptRate("0%");
        problem.setSubmissionCount(0);
        problem.setStatus(ProblemStatusEnum.UNSOLVED.getValue());  // 使用枚举值

        boolean saveResult = problemMapper.insert(problem) > 0;
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存题目失败");
        }

        // 2. 创建判断题详细信息
        JudgeProblem judgeProblem = new JudgeProblem();
        judgeProblem.setId(problem.getId());
        judgeProblem.setAnswer(judgeAddRequest.getAnswer());
        judgeProblem.setAnalysis(judgeAddRequest.getAnalysis());

        saveResult = judgeProblemMapper.insert(judgeProblem) > 0;
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存判断题详情失败");
        }

        return problem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addProgramProblem(ProgramProblemAddRequest request, Long userId) {
        try{
        // 1. 保存基本信息
        Problem problem = new Problem();
        BeanUtils.copyProperties(request, problem);
        problem.setUserId(userId);
        problem.setAcceptRate("0%");
        problem.setSubmissionCount(0);
        problem.setType("PROGRAM");
        problem.setStatus(ProblemStatusEnum.UNSOLVED.getValue());  // 使用枚举值

        // 处理标签 - 确保标签不为null
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            problem.setTags(String.join(",", request.getTags()));
        } else {
            problem.setTags(""); // 设置空字符串而不是null
        }

        if (problemMapper.insert(problem) <= 0) {
            throw new BusinessException(40001, "题目创建失败");
        }

        // 2. 保存编程题特有信息
        programProblem programProblem = new programProblem();
        programProblem.setId(problem.getId());
        programProblem.setFunctionName(request.getFunctionName());

        // 简化JSON处理 - 检查大小
        String paramTypesJson = JSONUtil.toJsonStr(request.getParamTypes());
        if (paramTypesJson.length() > 1000) {
            throw new BusinessException(40001, "参数类型数据过大");
        }
        programProblem.setParamTypes(paramTypesJson);

        programProblem.setReturnType(request.getReturnType());

        // 测试用例处理 - 检查大小
        String testCasesJson = JSONUtil.toJsonStr(request.getTestCases());
        if (testCasesJson.length() > 10000) {
            throw new BusinessException(40001, "测试用例数据过大，请减少测试用例数量或大小");
        }
        programProblem.setTestCases(testCasesJson);

        // 模板处理 - 检查大小
        Map<String, String> templates = request.getTemplates();
        String templatesJson = JSONUtil.toJsonStr(templates);
        if (templatesJson.length() > 20000) {
            throw new BusinessException(40001, "代码模板数据过大，请减少模板数量或大小");
        }
        programProblem.setTemplates(templatesJson);

        // 标准答案处理 - 检查大小
        Map<String, String> standardSolution = request.getStandardSolution();
        String standardSolutionJson = JSONUtil.toJsonStr(standardSolution);
        if (standardSolutionJson.length() > 20000) {
            throw new BusinessException(40001, "标准答案数据过大，请减少答案数量或大小");
        }
        programProblem.setStandardSolution(standardSolutionJson);

        programProblem.setTimeLimit(request.getTimeLimit());
        programProblem.setMemoryLimit(request.getMemoryLimit());

        if (programProblemMapper.insert(programProblem) <= 0) {
            throw new BusinessException(40001, "编程题详情创建失败");
        }

        return problem.getId();
    }catch(Exception e){// 记录详细异常信息
        e.printStackTrace();
        throw new BusinessException(40001, "添加编程题失败: " + e.getMessage());
    }
}


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProblem(Long id, Long userId) {
        // 校验题目是否存在且有权限
        Problem problem = validateProblem(id, userId);
        // 根据题目类型删除具体题型表中的数据
        String type = problem.getType();
        boolean specialTypeDeleted = false;
        
        log.info("开始删除题目, ID: {}, 类型: {}", id, type);
        
        // 尝试删除具体题型表中的数据，即使失败也继续删除problem表的数据
        try {
            switch (ProblemTypeEnum.valueOf(type)) {
                case CHOICE:
                    specialTypeDeleted = choiceProblemMapper.deleteById(id) > 0;
                    log.info("删除选择题详情表数据, 结果: {}", specialTypeDeleted);
                    break;
                case JUDGE:
                    specialTypeDeleted = judgeProblemMapper.deleteById(id) > 0;
                    log.info("删除判断题详情表数据, 结果: {}", specialTypeDeleted);
                    break;
                case PROGRAM:
                    specialTypeDeleted = programProblemMapper.deleteById(id) > 0;
                    log.info("删除编程题详情表数据, 结果: {}", specialTypeDeleted);
                    break;
                default:
                    log.warn("未知的题目类型: {}, ID: {}", type, id);
                    break;
            }
        } catch (Exception e) {
            // 仅记录错误日志，但不会中断删除流程
            log.error("删除题目特定类型数据失败, ID: {}, 类型: {}, 错误: {}", id, type, e.getMessage(), e);
        }
        
        if (!specialTypeDeleted) {
            log.warn("未能删除具体题型数据或具体题型数据不存在，仍会删除基本题目数据，题目ID: {}, 类型: {}", id, type);
        }
        
        // 删除题目基本信息 - 使用两种方式确保删除成功
        boolean problemDeleted = false;
        try {
            // 1. 使用 MyBatis-Plus 的删除方法（支持逻辑删除）
            problemDeleted = problemMapper.deleteById(id) > 0;
            log.info("方法1: MybatisPlus删除方法结果: {}", problemDeleted);
            
            // 2. 如果方法1失败，尝试手动更新 isDelete 字段
            if (!problemDeleted) {
                Problem updateProblem = new Problem();
                updateProblem.setId(id);
                updateProblem.setIsDelete(1);  // 标记为已删除
                problemDeleted = problemMapper.updateById(updateProblem) > 0;
                log.info("方法2: 手动更新isDelete字段结果: {}", problemDeleted);
            }
        } catch (Exception e) {
            log.error("删除题目基本数据异常, ID: {}, 错误: {}", id, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除题目失败: " + e.getMessage());
        }
        
        if (!problemDeleted) {
            log.error("删除题目基本数据失败, ID: {}", id);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除题目失败");
        }
        
        log.info("题目删除完成, ID: {}", id);
        return true;
    }

    @Override
    public ProblemVO getProblemById(Long id, Long userId) {
        // 1. 获取题目基本信息
        Problem problem = problemMapper.selectById(id);
        if (problem == null || problem.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 2. 转换为VO对象
        return problemToVO(problem, userId);
    }



    @Override
    public ProblemDTO getProblemDetail(Long id, Long userId) {
        log.info("开始获取题目详情, ID: {}, 用户ID: {}", id, userId);
        
        // 1. 获取题目基本信息
        Problem problem = validateProblem(id, userId);
        log.info("题目基本信息验证通过, 题目ID: {}, 类型: {}", id, problem.getType());
        
        // 2. 转换为DTO
        ProblemDTO problemDTO = new ProblemDTO();
        BeanUtils.copyProperties(problem, problemDTO);

        // 3. 获取创建者信息
        User creator = userService.getById(problem.getUserId());
        problemDTO.setCreatorName(creator != null ? creator.getUserName() : null);
        log.info("已设置创建者信息, 题目ID: {}, 创建者: {}", id, problemDTO.getCreatorName());

        // 4. 获取具体题目内容
        try {
            log.info("开始获取题目[{}]详情, 类型: {}", id, problem.getType());
            Object detail = getProblemDetailById(id, problem.getType());
            
            if (detail == null) {
                log.warn("题目[{}]详情为空", id);
                problemDTO.setProblemDetail("{}");
            } else {
                log.info("获取到题目详情对象类型: {}", detail.getClass().getName());
                try {
                    String detailJson = objectMapper.writeValueAsString(detail);
                    log.info("题目详情序列化成功, 长度: {}", detailJson.length());
                    if (detailJson.length() < 500) {
                        log.info("题目详情内容: {}", detailJson);
                    } else {
                        log.info("题目详情内容过长，截取部分显示: {}", detailJson.substring(0, 500) + "...");
                    }
                    problemDTO.setProblemDetail(detailJson);
                } catch (JsonProcessingException e) {
                    log.error("序列化题目详情失败, 题目ID: {}, 类型: {}, 详情类型: {}, 错误: {}", 
                              id, problem.getType(), detail.getClass().getName(), e.getMessage(), e);
                    
                    // 尝试使用其他序列化方式
                    try {
                        log.info("尝试使用备用序列化方式");
                        String detailJson = JSONUtil.toJsonStr(detail);
                        log.info("备用序列化成功, 长度: {}", detailJson.length());
                        problemDTO.setProblemDetail(detailJson);
                    } catch (Exception ex) {
                        log.error("备用序列化也失败, 题目ID: {}", id, ex);
                        problemDTO.setProblemDetail("{}");
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "序列化题目详情失败: " + e.getMessage());
                    }
                }
            }
        } catch (BusinessException e) {
            // 重新抛出业务异常
            log.error("获取题目详情业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取题目详情失败, 题目ID: {}, 类型: {}, 错误: {}", id, problem.getType(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取题目详情失败: " + e.getMessage());
        }

        log.info("题目详情获取成功, ID: {}", id);
        return problemDTO;
    }

    @Override
    public Problem validateProblem(Long id, Long userId) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Problem problem = problemMapper.selectById(id);
        if (problem == null || problem.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = UserContext.getUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请先登录");
        }
        
        // 判断操作类型 - 如果是修改题目等操作，仍然需要验证权限
        if (userId == null || userId <= 0) {
            // 如果只是查看题目详情，则任何登录用户都可以访问
            return problem;
        }
        
        // 如果是修改操作，仅创建者和管理员可以操作
        if (!loginUser.getUserRole().equals("admin") && !problem.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return problem;
    }


    /**
     * Problem 转 VO
     */
    @Override
    public ProblemVO problemToVO(Problem problem, Long userId) {
        if (problem == null) {
            return null;
        }

        ProblemVO problemVO = new ProblemVO();
        BeanUtils.copyProperties(problem, problemVO);

        // 处理标签
        if (StringUtils.isNotBlank(problem.getTags())) {
            problemVO.setTags(Arrays.asList(problem.getTags().split(",")));
        }

        // 如果是编程题且当前用户是创建者，则添加标准答案
        if ("PROGRAM".equals(problem.getType()) && userId != null && userId.equals(problem.getUserId())) {
            programProblem programProblem = programProblemMapper.selectById(problem.getId());
            if (programProblem != null && StringUtils.isNotBlank(programProblem.getStandardSolution())) {
                try {
                    Map<String, String> standardSolution = objectMapper.readValue(
                        programProblem.getStandardSolution(), 
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class)
                    );
                    problemVO.setStandardSolution(standardSolution);
                } catch (JsonProcessingException e) {
                    log.error("解析标准答案失败", e);
                }
            }
        }

        return problemVO;
    }

    /**
     * 保存具体题目内容
     */
    private void saveProblemDetail(Long problemId, String type, String detailJson) throws JsonProcessingException {
        if (StringUtils.isBlank(detailJson)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目详情不能为空");
        }

        switch (ProblemTypeEnum.valueOf(type)) {
            case CHOICE:
                ChoiceProblem choiceProblem = objectMapper.readValue(detailJson, ChoiceProblem.class);
                choiceProblem.setId(problemId);
                validateChoiceProblem(choiceProblem);
                choiceProblemMapper.insert(choiceProblem);
                break;
            case JUDGE:
                JudgeProblem judgeProblem = objectMapper.readValue(detailJson, JudgeProblem.class);
                judgeProblem.setId(problemId);
                validateJudgeProblem(judgeProblem);
                judgeProblemMapper.insert(judgeProblem);
                break;
            case PROGRAM:
                programProblem programProblem = objectMapper.readValue(detailJson, programProblem.class);
                programProblem.setId(problemId);
                validateprogramProblem(programProblem);
                programProblemMapper.insert(programProblem);
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的题目类型");
        }
    }

    /**
     * 更新具体题目内容
     */
    private void updateProblemDetail(Long problemId, String type, String detailJson) throws JsonProcessingException {
        if (StringUtils.isBlank(detailJson)) {
            return;
        }

        switch (ProblemTypeEnum.valueOf(type)) {
            case CHOICE:
                ChoiceProblem choiceProblem = objectMapper.readValue(detailJson, ChoiceProblem.class);
                choiceProblem.setId(problemId);
                validateChoiceProblem(choiceProblem);
                choiceProblemMapper.updateById(choiceProblem);
                break;
            case JUDGE:
                JudgeProblem judgeProblem = objectMapper.readValue(detailJson, JudgeProblem.class);
                judgeProblem.setId(problemId);
                validateJudgeProblem(judgeProblem);
                judgeProblemMapper.updateById(judgeProblem);
                break;
            case PROGRAM:
                programProblem programProblem = objectMapper.readValue(detailJson, programProblem.class);
                programProblem.setId(problemId);
                validateprogramProblem(programProblem);
                programProblemMapper.updateById(programProblem);
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的题目类型");
        }
    }

    /**
     * 获取具体题目内容
     */
    private Object getProblemDetailById(Long problemId, String type) {
        try {
            log.info("开始获取题目详情, ID: {}, 类型: {}", problemId, type);
            
            switch (ProblemTypeEnum.valueOf(type)) {
                case CHOICE:
                    log.info("处理选择题详情, ID: {}", problemId);
                    // 修改选择题详情获取逻辑
                    ChoiceProblem choiceProblem = choiceProblemMapper.selectById(problemId);
                    if (choiceProblem == null) {
                        log.warn("选择题不存在, ID: {}", problemId);
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "选择题不存在");
                    }
                    
                    log.info("成功获取选择题数据, ID: {}, answer: {}", problemId, choiceProblem.getAnswer());
                    
                    // 将选择题转换为DTO
                    ChoiceProblemDTO dto = new ChoiceProblemDTO();
                    BeanUtils.copyProperties(choiceProblem, dto);
                    
                    // 处理options字段 - 从JSON字符串转为对象列表
                    if (choiceProblem.getOptions() != null && !choiceProblem.getOptions().isEmpty()) {
                        log.info("开始解析选项数据, 数据: {}", choiceProblem.getOptions());
                        try {
                            List<ProblemOptionDTO> options = objectMapper.readValue(
                                choiceProblem.getOptions(),
                                new TypeReference<List<ProblemOptionDTO>>() {}
                            );
                            
                            // 自动生成选项键
                            char keyChar = 'A';
                            for (ProblemOptionDTO option : options) {
                                if (StringUtils.isBlank(option.getKey())) {
                                    option.setKey(String.valueOf(keyChar));
                                    log.info("为选项自动生成键: {}", keyChar);
                                }
                                keyChar++;
                            }
                            
                            log.info("选项解析成功, 选项数量: {}", options.size());
                            dto.setOptions(options);
                        } catch (Exception e) {
                            log.error("解析选择题选项失败, 题目ID: {}, 选项数据: {}, 异常: {}", 
                                     problemId, choiceProblem.getOptions(), e.getMessage(), e);
                            // 设置为空列表而不是抛出异常
                            dto.setOptions(new ArrayList<>());
                        }
                    } else {
                        log.warn("选择题选项为空, ID: {}", problemId);
                        dto.setOptions(new ArrayList<>());
                    }
                    
                    log.info("选择题详情处理完成, ID: {}", problemId);
                    return dto;
                    
                case JUDGE:
                    log.info("处理判断题详情, ID: {}", problemId);
                    JudgeProblem judgeProblem = judgeProblemMapper.selectById(problemId);
                    if (judgeProblem == null) {
                        log.warn("判断题不存在, ID: {}", problemId);
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "判断题不存在");
                    }
                    return judgeProblem;
                    
                case PROGRAM:
                    log.info("处理编程题详情, ID: {}", problemId);
                    programProblem programProblem = programProblemMapper.selectById(problemId);
                    if (programProblem == null) {
                        log.warn("编程题不存在, ID: {}", problemId);
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "编程题不存在");
                    }
                    return programProblem;
                    
                default:
                    log.warn("不支持的题目类型: {}", type);
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的题目类型");
            }
        } catch (IllegalArgumentException e) {
            log.error("题目类型转换错误: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的题目类型: " + type);
        } catch (Exception e) {
            log.error("获取题目详情失败, ID: {}, 类型: {}, 异常: {}", problemId, type, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取题目详情失败: " + e.getMessage());
        }
    }

    /**
     * 校验选择题
     */
    private void validateChoiceProblem(ChoiceProblem choiceProblem) {
        if (choiceProblem == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(choiceProblem.getOptions())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "选项不能为空");
        }
        if (StringUtils.isBlank(choiceProblem.getAnswer())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案不能为空");
        }
    }

    /**
     * 校验判断题
     */
    private void validateJudgeProblem(JudgeProblem judgeProblem) {
        if (judgeProblem == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (judgeProblem.getAnswer() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案不能为空");
        }
    }

    /**
     * 校验编程题
     */
    private void validateprogramProblem(programProblem programProblem) {
        if (programProblem == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(programProblem.getFunctionName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "函数名称不能为空");
        }
        if (StringUtils.isBlank(programProblem.getParamTypes())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数类型不能为空");
        }
        if (StringUtils.isBlank(programProblem.getReturnType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "返回值类型不能为空");
        }
        if (StringUtils.isBlank(programProblem.getTestCases())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "测试用例不能为空");
        }
    }

    /**
     * 格式化标签
     */
    private String formatTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        
        Set<String> tagSet = tags.stream()
                .filter(tag -> StringUtils.isNotBlank(tag))
                .map(String::trim)
                .collect(Collectors.toSet());
        
        if (tagSet.size() > ProblemConstant.MAX_TAGS_COUNT) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签数量不能超过" + ProblemConstant.MAX_TAGS_COUNT);
        }
        
        return String.join(",", tagSet);
    }

    /**
     * 格式化标签（字符串输入）
     */
    private String formatTags(String tags) {
        if (StringUtils.isBlank(tags)) {
            return "";
        }
        String[] tagArray = tags.split(",");
        return formatTags(Arrays.asList(tagArray));
    }

    @Override
    public void updateProblemSubmitInfo(Long problemId) {
        if (problemId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        
        // 查询提交记录统计信息
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getProblemId, problemId);
        long totalSubmissions = submissionMapper.selectCount(wrapper);
        
        // 查询通过的提交数
        wrapper.eq(Submission::getStatus, SubmissionStatusEnum.ACCEPTED.name());
        long acceptedSubmissions = submissionMapper.selectCount(wrapper);
        
        // 计算通过率
        String acceptRate = totalSubmissions > 0 
            ? String.format("%.1f%%", (acceptedSubmissions * 100.0) / totalSubmissions)
            : "0%";
        
        // 更新题目信息
        problem.setSubmissionCount((int) totalSubmissions);
        problem.setAcceptRate(acceptRate);
        problemMapper.updateById(problem);
    }

    @Override
    public List<ProblemVO> getProblemsByIds(List<Long> problemIds) {
        if (CollectionUtils.isEmpty(problemIds)) {
            return new ArrayList<>();
        }
        return problemMapper.selectBatchIds(problemIds).stream()
                .map(problem -> problemToVO(problem, null))
                .collect(Collectors.toList());
    }

    @Override
    public ProblemDTO problemToDTO(Problem problem) {
        if (problem == null) {
            return null;
        }
        ProblemDTO problemDTO = new ProblemDTO();
        BeanUtils.copyProperties(problem, problemDTO);
        return problemDTO;
    }

    @Override
    public ProblemVO dtoToVO(ProblemDTO problemDTO) {
        if (problemDTO == null) {
            return null;
        }
        ProblemVO problemVO = new ProblemVO();
        BeanUtils.copyProperties(problemDTO, problemVO);
        if (StringUtils.isNotBlank(problemDTO.getTags())) {
            problemVO.setTags(Arrays.asList(problemDTO.getTags().split(",")));
        }
        return problemVO;
    }

    @Override
    public List<ProblemVO> getProblemsByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<Problem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Problem::getUserId, userId);
        queryWrapper.eq(Problem::getIsDelete, 0); // 确保只查询未删除的题目
        List<Problem> problems = problemMapper.selectList(queryWrapper);
        return problems.stream()
                .map(problem -> problemToVO(problem, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProblemVO> searchProblems(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Problem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Problem::getTitle, keyword)
                    .or()
                    .like(Problem::getContent, keyword)
                    .eq(Problem::getIsDelete, 0); // 确保只查询未删除的题目
        List<Problem> problems = problemMapper.selectList(queryWrapper);
        return problems.stream()
                .map(problem -> problemToVO(problem, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateChoiceProblem(ChoiceProblemUpdateRequest request, Long userId) {
        try {
            // 1. 校验题目是否存在且有权限
            Problem oldProblem = validateProblem(request.getId(), userId);
            if (!"CHOICE".equals(oldProblem.getType())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目类型不匹配");
            }

            // 2. 更新基本信息
            Problem problem = new Problem();
            BeanUtils.copyProperties(request, problem);
            problem.setStatus(oldProblem.getStatus());  // 保留原有状态
            
            // 处理标签
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                problem.setTags(formatTags(request.getTags()));
            }

            if (problemMapper.updateById(problem) <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新题目基本信息失败");
            }

            // 3. 更新选择题特有信息
            ChoiceProblem choiceProblem = new ChoiceProblem();
            choiceProblem.setId(request.getId());
            choiceProblem.setOptions(JSONUtil.toJsonStr(request.getOptions()));
            choiceProblem.setAnswer(request.getAnswer());
            choiceProblem.setAnalysis(request.getAnalysis());

            if (choiceProblemMapper.updateById(choiceProblem) <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新选择题详情失败");
            }

            return true;
        } catch (BusinessException e) {
            if (e.getCode() == ErrorCode.NOT_FOUND_ERROR.getCode()) {
                // 如果题目不存在，尝试添加新题目
                ChoiceProblemAddRequest addRequest = new ChoiceProblemAddRequest();
                BeanUtils.copyProperties(request, addRequest);
                addChoiceProblem(addRequest, userId);
                return true;
            }
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJudgeProblem(JudgeProblemUpdateRequest request, Long userId) {
        try {
            // 1. 校验题目是否存在且有权限
            Problem oldProblem = validateProblem(request.getId(), userId);
            if (!"JUDGE".equals(oldProblem.getType())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目类型不匹配");
            }

            // 2. 更新基本信息
            Problem problem = new Problem();
            BeanUtils.copyProperties(request, problem);
            problem.setStatus(oldProblem.getStatus());  // 保留原有状态
            
            // 处理标签
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                problem.setTags(formatTags(request.getTags()));
            }

            if (problemMapper.updateById(problem) <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新题目基本信息失败");
            }

            // 3. 更新判断题特有信息
            JudgeProblem judgeProblem = new JudgeProblem();
            judgeProblem.setId(request.getId());
            judgeProblem.setAnswer(request.getAnswer());
            judgeProblem.setAnalysis(request.getAnalysis());

            if (judgeProblemMapper.updateById(judgeProblem) <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新判断题详情失败");
            }

            return true;
        } catch (BusinessException e) {
            if (e.getCode() == ErrorCode.NOT_FOUND_ERROR.getCode()) {
                // 如果题目不存在，尝试添加新题目
                JudgeProblemAddRequest addRequest = new JudgeProblemAddRequest();
                BeanUtils.copyProperties(request, addRequest);
                addJudgeProblem(addRequest, userId);
                return true;
            }
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProgramProblem(ProgramProblemUpdateRequest request, Long userId) {
        try {
            // 1. 校验题目是否存在且有权限
            Problem oldProblem = validateProblem(request.getId(), userId);
            if (!"PROGRAM".equals(oldProblem.getType())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目类型不匹配");
            }

            // 2. 更新基本信息
            Problem problem = new Problem();
            BeanUtils.copyProperties(request, problem);
            problem.setStatus(oldProblem.getStatus());  // 保留原有状态
            
            // 处理标签
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                problem.setTags(formatTags(request.getTags()));
            }

            if (problemMapper.updateById(problem) <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新题目基本信息失败");
            }

            // 3. 更新编程题特有信息
            programProblem programProblem = new programProblem();
            programProblem.setId(request.getId());
            programProblem.setFunctionName(request.getFunctionName());
            programProblem.setParamTypes(JSONUtil.toJsonStr(request.getParamTypes()));
            programProblem.setReturnType(request.getReturnType());
            programProblem.setTestCases(JSONUtil.toJsonStr(request.getTestCases()));
            programProblem.setTemplates(JSONUtil.toJsonStr(request.getTemplates()));
            programProblem.setStandardSolution(JSONUtil.toJsonStr(request.getStandardSolution()));
            programProblem.setTimeLimit(request.getTimeLimit());
            programProblem.setMemoryLimit(request.getMemoryLimit());

            if (programProblemMapper.updateById(programProblem) <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新编程题详情失败");
            }

            return true;
        } catch (BusinessException e) {
            if (e.getCode() == ErrorCode.NOT_FOUND_ERROR.getCode()) {
                // 如果题目不存在，尝试添加新题目
                ProgramProblemAddRequest addRequest = new ProgramProblemAddRequest();
                BeanUtils.copyProperties(request, addRequest);
                addProgramProblem(addRequest, userId);
                return true;
            }
            throw e;
        }
    }

    @Override
    public Page<ProblemVO> listProblemByPage(ProblemQueryRequest problemQueryRequest, Long userId) {
        if (problemQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 获取分页参数
        long current = problemQueryRequest.getCurrent();
        long size = problemQueryRequest.getPageSize();
        
        // 构建查询条件
        LambdaQueryWrapper<Problem> queryWrapper = buildQueryWrapper(problemQueryRequest);
        
        // 执行分页查询
        Page<Problem> problemPage = new Page<>(current, size);
        Page<Problem> resultPage = problemMapper.selectPage(problemPage, queryWrapper);
        
        // 转换结果
        List<ProblemVO> problemVOList = resultPage.getRecords().stream()
                .map(problem -> problemToVO(problem, userId))
                .collect(Collectors.toList());
        
        // 构建返回结果
        Page<ProblemVO> problemVOPage = new Page<>(current, size, resultPage.getTotal());
        problemVOPage.setRecords(problemVOList);
        
        return problemVOPage;
    }
    
    /**
     * 构建查询wrapper
     */
    private LambdaQueryWrapper<Problem> buildQueryWrapper(ProblemQueryRequest request) {
        LambdaQueryWrapper<Problem> queryWrapper = new LambdaQueryWrapper<>();
        
        // 默认查询未删除的题目
        queryWrapper.eq(Problem::getIsDelete, 0);
        
        if (request == null) {
            return queryWrapper;
        }
        
        // 根据ID查询
        Long id = request.getId();
        if (id != null && id > 0) {
            queryWrapper.eq(Problem::getId, id);
            return queryWrapper;
        }
        
        // 根据搜索文本查询（标题或内容）
        String searchText = request.getSearchText();
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(wrapper -> 
                wrapper.like(Problem::getTitle, searchText)
                       .or()
                       .like(Problem::getContent, searchText)
            );
        }
        
        // 根据题目类型查询
        String type = request.getType();
        if (StringUtils.isNotBlank(type)) {
            queryWrapper.eq(Problem::getType, type);
        }
        
        // 根据岗位类型查询
        String jobType = request.getJobType();
        if (StringUtils.isNotBlank(jobType)) {
            queryWrapper.eq(Problem::getJobType, jobType);
        }
        
        // 根据难度查询
        String difficulty = request.getDifficulty();
        if (StringUtils.isNotBlank(difficulty)) {
            queryWrapper.eq(Problem::getDifficulty, difficulty);
        }
        
        // 根据标签查询
        List<String> tags = request.getTags();
        if (tags != null && !tags.isEmpty()) {
            // 标签是以逗号分隔的字符串，使用模糊查询每个标签
            for (String tag : tags) {
                if (StringUtils.isNotBlank(tag)) {
                    queryWrapper.like(Problem::getTags, tag);
                }
            }
        }
        
        // 根据创建者ID查询
        Long userId = request.getUserId();
        if (userId != null && userId > 0) {
            queryWrapper.eq(Problem::getUserId, userId);
        }
        
        // 根据状态查询
        String status = request.getStatus();
        if (StringUtils.isNotBlank(status)) {
            try {
                // 验证状态值是否合法
                if (ProblemStatusEnum.getByValue(status) != null) {
                    queryWrapper.eq(Problem::getStatus, status);
                } else {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的题目状态");
                }
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的题目状态");
            }
        }
        
        // 排序处理
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        
        if (StringUtils.isNotBlank(sortField)) {
            // 根据不同字段排序
            if ("createTime".equals(sortField)) {
                queryWrapper.orderBy(true, "ascend".equals(sortOrder), Problem::getCreateTime);
            } else if ("updateTime".equals(sortField)) {
                queryWrapper.orderBy(true, "ascend".equals(sortOrder), Problem::getUpdateTime);
            } else if ("submissionCount".equals(sortField)) {
                queryWrapper.orderBy(true, "ascend".equals(sortOrder), Problem::getSubmissionCount);
            } else if ("difficulty".equals(sortField)) {
                // 难度排序，需要自定义排序规则
                if ("ascend".equals(sortOrder)) {
                    queryWrapper.last("ORDER BY CASE difficulty " +
                            "WHEN 'EASY' THEN 1 " +
                            "WHEN 'MEDIUM' THEN 2 " +
                            "WHEN 'HARD' THEN 3 " +
                            "ELSE 4 END ASC");
                } else {
                    queryWrapper.last("ORDER BY CASE difficulty " +
                            "WHEN 'EASY' THEN 1 " +
                            "WHEN 'MEDIUM' THEN 2 " +
                            "WHEN 'HARD' THEN 3 " +
                            "ELSE 4 END DESC");
                }
            }
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc(Problem::getCreateTime);
        }
        
        return queryWrapper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProblemStatus(Long problemId, Long userId, String status) {
        // 参数校验
        if (problemId == null || problemId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 验证状态值是否合法
        try {
            if (ProblemStatusEnum.getByValue(status) == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的题目状态");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的题目状态");
        }
        
        // 查询题目是否存在
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null || problem.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        
        // 更新状态
        Problem updateProblem = new Problem();
        updateProblem.setId(problemId);
        updateProblem.setStatus(status);
        
        boolean result = problemMapper.updateById(updateProblem) > 0;
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新题目状态失败");
        }
        
        return true;
    }

    @Override
    public ProblemVO getRandomProblem(Long userId) {
        // 查询可用题目总数
        LambdaQueryWrapper<Problem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Problem::getIsDelete, 0);
        Long count = problemMapper.selectCount(queryWrapper);
        
        if (count == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "暂无可用题目");
        }
        
        // 随机获取一题
        long randomOffset = (long) (Math.random() * count);
        Page<Problem> page = new Page<>(randomOffset, 1);
        Page<Problem> problemPage = problemMapper.selectPage(page, queryWrapper);
        
        if (problemPage.getRecords().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取随机题目失败");
        }
        
        // 转换为VO
        Problem randomProblem = problemPage.getRecords().get(0);
        return problemToVO(randomProblem, userId);
    }
    
    @Override
    public ProblemVO getDailyProblem(Long userId) {
        // 获取当前日期
        String today = java.time.LocalDate.now().toString();
        
        // 基于当前日期生成伪随机数
        int seed = today.hashCode();
        Random random = new Random(seed);
        
        // 查询可用题目总数
        LambdaQueryWrapper<Problem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Problem::getIsDelete, 0);
        Long count = problemMapper.selectCount(queryWrapper);
        
        if (count == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "暂无可用题目");
        }
        
        // 基于日期随机选择一题
        long index = Math.abs(random.nextLong()) % count;
        Page<Problem> page = new Page<>(index + 1, 1);
        Page<Problem> problemPage = problemMapper.selectPage(page, queryWrapper);
        
        if (problemPage.getRecords().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取每日一题失败");
        }
        
        // 转换为VO
        Problem dailyProblem = problemPage.getRecords().get(0);
        return problemToVO(dailyProblem, userId);
    }

    @Override
    public Page<ProblemVO> searchProblemAdvanced(ProblemSearchRequest problemSearchRequest, Long userId) {
        if (problemSearchRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 获取分页参数
        long current = problemSearchRequest.getCurrent();
        long size = problemSearchRequest.getPageSize();
        
        // 构建查询条件
        LambdaQueryWrapper<Problem> queryWrapper = new LambdaQueryWrapper<>();
        
        // 默认查询未删除的题目
        queryWrapper.eq(Problem::getIsDelete, 0);
        
        // 处理关键词搜索（支持多关键词）
        String keywords = problemSearchRequest.getKeywords();
        if (StringUtils.isNotBlank(keywords)) {
            // 拆分关键词
            String[] keywordArray = keywords.split(",");
            for (String keyword : keywordArray) {
                if (StringUtils.isNotBlank(keyword)) {
                    queryWrapper.and(wrapper -> 
                        wrapper.like(Problem::getTitle, keyword)
                               .or()
                               .like(Problem::getContent, keyword)
                               .or()
                               .like(Problem::getTags, keyword)
                    );
                }
            }
        }
        
        // 处理题目类型列表
        List<String> typeList = problemSearchRequest.getTypeList();
        if (typeList != null && !typeList.isEmpty()) {
            queryWrapper.in(Problem::getType, typeList);
        }
        
        // 处理难度列表
        List<String> difficultyList = problemSearchRequest.getDifficultyList();
        if (difficultyList != null && !difficultyList.isEmpty()) {
            queryWrapper.in(Problem::getDifficulty, difficultyList);
        }
        
        // 处理岗位列表
        List<String> jobTypeList = problemSearchRequest.getJobTypeList();
        if (jobTypeList != null && !jobTypeList.isEmpty()) {
            queryWrapper.in(Problem::getJobType, jobTypeList);
        }
        
        // 处理状态列表
        List<String> statusList = problemSearchRequest.getStatusList();
        if (statusList != null && !statusList.isEmpty()) {
            queryWrapper.in(Problem::getStatus, statusList);
        }
        
        // 处理标签列表
        List<String> tags = problemSearchRequest.getTags();
        if (tags != null && !tags.isEmpty()) {
            // 标签是以逗号分隔的字符串，使用模糊查询每个标签
            for (String tag : tags) {
                if (StringUtils.isNotBlank(tag)) {
                    queryWrapper.like(Problem::getTags, tag);
                }
            }
        }
        
        // 处理创建者ID列表
        List<Long> creatorIds = problemSearchRequest.getCreatorIds();
        if (creatorIds != null && !creatorIds.isEmpty()) {
            queryWrapper.in(Problem::getUserId, creatorIds);
        }
        
        // 处理是否只看自己的题目
        Boolean onlyMine = problemSearchRequest.getOnlyMine();
        if (onlyMine != null && onlyMine && userId != null) {
            queryWrapper.eq(Problem::getUserId, userId);
        }
        
        // 处理提交数范围
        Integer minSubmissionCount = problemSearchRequest.getMinSubmissionCount();
        if (minSubmissionCount != null && minSubmissionCount > 0) {
            queryWrapper.ge(Problem::getSubmissionCount, minSubmissionCount);
        }
        
        Integer maxSubmissionCount = problemSearchRequest.getMaxSubmissionCount();
        if (maxSubmissionCount != null && maxSubmissionCount > 0) {
            queryWrapper.le(Problem::getSubmissionCount, maxSubmissionCount);
        }
        
        // 通过率范围处理较复杂，需要先预处理
        Integer minAcceptRate = problemSearchRequest.getMinAcceptRate();
        Integer maxAcceptRate = problemSearchRequest.getMaxAcceptRate();
        if (minAcceptRate != null || maxAcceptRate != null) {
            // 由于通过率是字符串类型，这里只能使用子查询或自定义SQL，或者全量查询后在应用层过滤
            // 这里采用简单方式，先查询所有符合条件的题目，然后在应用层过滤
            queryWrapper.and(wrapper -> wrapper.isNotNull(Problem::getAcceptRate));
        }
        
        // 排序处理
        String sortField = problemSearchRequest.getSortField();
        String sortOrder = problemSearchRequest.getSortOrder();
        
        if (StringUtils.isNotBlank(sortField)) {
            // 根据不同字段排序
            if ("createTime".equals(sortField)) {
                queryWrapper.orderBy(true, "ascend".equals(sortOrder), Problem::getCreateTime);
            } else if ("updateTime".equals(sortField)) {
                queryWrapper.orderBy(true, "ascend".equals(sortOrder), Problem::getUpdateTime);
            } else if ("submissionCount".equals(sortField)) {
                queryWrapper.orderBy(true, "ascend".equals(sortOrder), Problem::getSubmissionCount);
            } else if ("difficulty".equals(sortField)) {
                // 难度排序，需要自定义排序规则
                if ("ascend".equals(sortOrder)) {
                    queryWrapper.last("ORDER BY CASE difficulty " +
                            "WHEN 'EASY' THEN 1 " +
                            "WHEN 'MEDIUM' THEN 2 " +
                            "WHEN 'HARD' THEN 3 " +
                            "ELSE 4 END ASC");
                } else {
                    queryWrapper.last("ORDER BY CASE difficulty " +
                            "WHEN 'EASY' THEN 1 " +
                            "WHEN 'MEDIUM' THEN 2 " +
                            "WHEN 'HARD' THEN 3 " +
                            "ELSE 4 END DESC");
                }
            }
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc(Problem::getCreateTime);
        }
        
        // 执行分页查询
        Page<Problem> problemPage = new Page<>(current, size);
        Page<Problem> resultPage = problemMapper.selectPage(problemPage, queryWrapper);
        
        // 处理通过率过滤
        List<Problem> filteredRecords = resultPage.getRecords();
        if (minAcceptRate != null || maxAcceptRate != null) {
            filteredRecords = resultPage.getRecords().stream()
                .filter(problem -> {
                    if (StringUtils.isBlank(problem.getAcceptRate())) {
                        return false;
                    }
                    
                    try {
                        // 从"XX.X%" 格式转换为浮点数
                        String rateStr = problem.getAcceptRate().replaceAll("%", "");
                        double rate = Double.parseDouble(rateStr);
                        
                        if (minAcceptRate != null && rate < minAcceptRate) {
                            return false;
                        }
                        
                        if (maxAcceptRate != null && rate > maxAcceptRate) {
                            return false;
                        }
                        
                        return true;
                    } catch (NumberFormatException e) {
                        log.warn("通过率格式异常: {}", problem.getAcceptRate());
                        return false;
                    }
                })
                .collect(Collectors.toList());
        }
        
        // 转换结果
        List<ProblemVO> problemVOList = filteredRecords.stream()
                .map(problem -> problemToVO(problem, userId))
                .collect(Collectors.toList());
        
        // 构建返回结果
        Page<ProblemVO> problemVOPage = new Page<>(current, size, resultPage.getTotal());
        problemVOPage.setRecords(problemVOList);
        
        return problemVOPage;
    }

    /**
     * 验证选择题添加请求参数
     */
    private void validateChoiceProblemRequest(ChoiceProblemAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 验证基本字段
        if (StringUtils.isBlank(request.getTitle()) || 
            StringUtils.isBlank(request.getContent()) || 
            StringUtils.isBlank(request.getDifficulty()) ||
            StringUtils.isBlank(request.getType()) ||
            StringUtils.isBlank(request.getJobType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必填字段不能为空");
        }
        
        // 验证选项
        List<ProblemOptionDTO> options = request.getOptions();
        if (options == null || options.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "选项不能为空");
        }
        
        // 验证选项内容不能为空
        boolean hasEmptyContent = options.stream()
                .anyMatch(option -> StringUtils.isBlank(option.getContent()));
        if (hasEmptyContent) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "选项内容不能为空");
        }
        
        // 验证答案
        if (StringUtils.isBlank(request.getAnswer())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案不能为空");
        }
        
        // 自动补充选项键值
        // 移除强制验证选项键，改为自动生成
        char keyChar = 'A';
        for (ProblemOptionDTO option : options) {
            if (StringUtils.isBlank(option.getKey())) {
                option.setKey(String.valueOf(keyChar));
            }
            keyChar++;
        }
    }
} 