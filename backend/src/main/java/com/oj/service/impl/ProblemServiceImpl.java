package com.oj.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.common.ErrorCode;
import com.oj.common.UserContext;
import com.oj.constant.ProblemConstant;
import com.oj.exception.BusinessException;
import com.oj.mapper.*;
import com.oj.model.dto.ProblemDTO;
import com.oj.model.entity.*;
import com.oj.model.enums.ProblemTypeEnum;
import com.oj.model.enums.SubmissionStatusEnum;
import com.oj.model.request.*;
import com.oj.model.vo.ProblemVO;
import com.oj.service.ProblemService;
import com.oj.service.UserService;
import com.oj.utils.ValidateUtils;
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
    private ProgrammingProblemMapper programmingProblemMapper;

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
        // 1. 保存基本信息
        Problem problem = new Problem();
        BeanUtils.copyProperties(request, problem);
        problem.setUserId(userId);
        problem.setAcceptRate("0%");
        problem.setSubmissionCount(0);
        problem.setType("CHOICE");

        // 处理标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            problem.setTags(formatTags(request.getTags()));
        }

        if (problemMapper.insert(problem) <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目创建失败");
        }

        // 2. 保存选择题特有信息
        ChoiceProblem choiceProblem = new ChoiceProblem();
        choiceProblem.setId(problem.getId());
        choiceProblem.setOptions(JSONUtil.toJsonStr(request.getOptions()));
        choiceProblem.setAnswer(request.getAnswer());
        choiceProblem.setAnalysis(request.getAnalysis());

        if (choiceProblemMapper.insert(choiceProblem) <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "选择题详情创建失败");
        }

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
    public Long addProgrammingProblem(ProgrammingProblemAddRequest request, Long userId) {
        // 1. 保存基本信息
        Problem problem = new Problem();
        BeanUtils.copyProperties(request, problem);
        problem.setUserId(userId);
        problem.setAcceptRate("0%");
        problem.setSubmissionCount(0);
        problem.setType("PROGRAM");

        // 处理标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            problem.setTags(formatTags(request.getTags()));
        }

        if (problemMapper.insert(problem) <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目创建失败");
        }

        // 2. 保存编程题特有信息
        ProgrammingProblem programmingProblem = new ProgrammingProblem();
        programmingProblem.setId(problem.getId());
        programmingProblem.setFunctionName(request.getFunctionName());
        programmingProblem.setParamTypes(JSONUtil.toJsonStr(request.getParamTypes()));
        programmingProblem.setReturnType(request.getReturnType());
        programmingProblem.setTestCases(JSONUtil.toJsonStr(request.getTestCases()));
        programmingProblem.setTemplates(JSONUtil.toJsonStr(request.getTemplates()));
        programmingProblem.setStandardSolution(JSONUtil.toJsonStr(request.getStandardSolution()));
        programmingProblem.setTimeLimit(request.getTimeLimit());
        programmingProblem.setMemoryLimit(request.getMemoryLimit());

        if (programmingProblemMapper.insert(programmingProblem) <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "编程题详情创建失败");
        }

        return problem.getId();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProblem(Long id, Long userId) {
        // 校验题目是否存在且有权限
        Problem problem = validateProblem(id, userId);
        // 根据题目类型删除具体题型表中的数据
        String type = problem.getType();
        boolean result = false;
        
        // 先删除具体题型表中的数据
        switch (ProblemTypeEnum.valueOf(type)) {
            case CHOICE:
                result = choiceProblemMapper.deleteById(id) > 0;
                break;
            case JUDGE:
                result = judgeProblemMapper.deleteById(id) > 0;
                break;
            case PROGRAM:
                result = programmingProblemMapper.deleteById(id) > 0;
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的题目类型");
        }
        
        if (!result) {
            log.warn("删除题目详情失败，题目ID: {}, 类型: {}", id, type);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除题目详情失败");
        }
        
        // 再删除题目基本信息
        if (problemMapper.deleteById(id) <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除题目失败");
        }
        
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
    public Page<ProblemVO> listProblem(ProblemQueryRequest request) {
        // 1. 创建查询条件
        LambdaQueryWrapper<Problem> queryWrapper = buildQueryWrapper(request);

        // 2. 分页查询
        Page<Problem> problemPage = new Page<>(request.getCurrent(), request.getPageSize());
        problemPage = problemMapper.selectPage(problemPage, queryWrapper);

        // 3. 转换为VO
        Page<ProblemVO> voPage = new Page<>(problemPage.getCurrent(), problemPage.getSize(), problemPage.getTotal());
        List<ProblemVO> problemVOList = problemPage.getRecords().stream()
                .map(problem -> problemToVO(problem, request.getUserId()))
                .collect(Collectors.toList());
        voPage.setRecords(problemVOList);

        return voPage;
    }

    @Override
    public ProblemDTO getProblemDetail(Long id, Long userId) {
        // 1. 获取题目基本信息
        Problem problem = validateProblem(id, userId);
        
        // 2. 转换为DTO
        ProblemDTO problemDTO = new ProblemDTO();
        BeanUtils.copyProperties(problem, problemDTO);

        // 3. 获取创建者信息
        User creator = userService.getById(problem.getUserId());
        problemDTO.setCreatorName(creator != null ? creator.getUserName() : null);

        // 4. 获取具体题目内容
        try {
            Object detail = getProblemDetailById(id, problem.getType());
            problemDTO.setProblemDetail((String) detail);
        } catch (Exception e) {
            log.error("获取题目详情失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取题目详情失败");
        }

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
        // 仅创建者和管理员可以操作
        if (!loginUser.getUserRole().equals("admin") &&!problem.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return problem;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Problem> buildQueryWrapper(ProblemQueryRequest request) {
        LambdaQueryWrapper<Problem> queryWrapper = new LambdaQueryWrapper<>();
        
        if (request != null) {
            // 标题模糊搜索
            if (StringUtils.isNotBlank(request.getTitle())) {
                queryWrapper.like(Problem::getTitle, request.getTitle());
            }

            // 岗位方向
            if (StringUtils.isNotBlank(request.getJobType())) {
                queryWrapper.eq(Problem::getJobType, request.getJobType());
            }

            // 状态
            if (StringUtils.isNotBlank(request.getStatus())) {
                queryWrapper.eq(Problem::getStatus, request.getStatus());
            }

            // 难度
            if (StringUtils.isNotBlank(request.getDifficulty())) {
                queryWrapper.eq(Problem::getDifficulty, request.getDifficulty());
            }

            // 标签查询（与查询）
            if (StringUtils.isNotBlank(request.getTags())) {
                String[] tagArray = request.getTags().split(",");
                for (String tag : tagArray) {
                    if (StringUtils.isNotBlank(tag)) {
                        queryWrapper.like(Problem::getTags, tag.trim());
                    }
                }
            }

            // 题目类型
            if (StringUtils.isNotBlank(request.getType())) {
                queryWrapper.eq(Problem::getType, request.getType());
            }

            // 创建者
            if (request.getUserId() != null) {
                queryWrapper.eq(Problem::getUserId, request.getUserId());
            }

            // 排序
            if (StringUtils.isNotBlank(request.getSortField())) {
                boolean isAsc = "ascend".equals(request.getSortOrder());
                switch (request.getSortField()) {
                    case "createTime":
                        queryWrapper.orderBy(true, isAsc, Problem::getCreateTime);
                        break;
                    case "updateTime":
                        queryWrapper.orderBy(true, isAsc, Problem::getUpdateTime);
                        break;
                    case "submissionCount":
                        queryWrapper.orderBy(true, isAsc, Problem::getSubmissionCount);
                        break;
                    case "acceptRate":
                        queryWrapper.orderBy(true, isAsc, Problem::getAcceptRate);
                        break;
                    default:
                        queryWrapper.orderBy(true, isAsc, Problem::getCreateTime);
                }
            } else {
                // 默认按创建时间降序
                queryWrapper.orderByDesc(Problem::getCreateTime);
            }
        }

        queryWrapper.eq(Problem::getIsDelete, 0);
        return queryWrapper;
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
            ProgrammingProblem programmingProblem = programmingProblemMapper.selectById(problem.getId());
            if (programmingProblem != null && StringUtils.isNotBlank(programmingProblem.getStandardSolution())) {
                try {
                    Map<String, String> standardSolution = objectMapper.readValue(
                        programmingProblem.getStandardSolution(), 
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
                ProgrammingProblem programmingProblem = objectMapper.readValue(detailJson, ProgrammingProblem.class);
                programmingProblem.setId(problemId);
                validateProgrammingProblem(programmingProblem);
                programmingProblemMapper.insert(programmingProblem);
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
                ProgrammingProblem programmingProblem = objectMapper.readValue(detailJson, ProgrammingProblem.class);
                programmingProblem.setId(problemId);
                validateProgrammingProblem(programmingProblem);
                programmingProblemMapper.updateById(programmingProblem);
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的题目类型");
        }
    }

    /**
     * 获取具体题目内容
     */
    private Object getProblemDetailById(Long problemId, String type) {
        switch (ProblemTypeEnum.valueOf(type)) {
            case CHOICE:
                return choiceProblemMapper.selectById(problemId);
            case JUDGE:
                return judgeProblemMapper.selectById(problemId);
            case PROGRAM:
                return programmingProblemMapper.selectById(problemId);
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的题目类型");
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
    private void validateProgrammingProblem(ProgrammingProblem programmingProblem) {
        if (programmingProblem == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(programmingProblem.getFunctionName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "函数名称不能为空");
        }
        if (StringUtils.isBlank(programmingProblem.getParamTypes())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数类型不能为空");
        }
        if (StringUtils.isBlank(programmingProblem.getReturnType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "返回值类型不能为空");
        }
        if (StringUtils.isBlank(programmingProblem.getTestCases())) {
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
    public boolean updateProgrammingProblem(ProgrammingProblemUpdateRequest request, Long userId) {
        try {
            // 1. 校验题目是否存在且有权限
            Problem oldProblem = validateProblem(request.getId(), userId);
            if (!"PROGRAM".equals(oldProblem.getType())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目类型不匹配");
            }

            // 2. 更新基本信息
            Problem problem = new Problem();
            BeanUtils.copyProperties(request, problem);
            
            // 处理标签
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                problem.setTags(formatTags(request.getTags()));
            }

            if (problemMapper.updateById(problem) <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新题目基本信息失败");
            }

            // 3. 更新编程题特有信息
            ProgrammingProblem programmingProblem = new ProgrammingProblem();
            programmingProblem.setId(request.getId());
            programmingProblem.setFunctionName(request.getFunctionName());
            programmingProblem.setParamTypes(JSONUtil.toJsonStr(request.getParamTypes()));
            programmingProblem.setReturnType(request.getReturnType());
            programmingProblem.setTestCases(JSONUtil.toJsonStr(request.getTestCases()));
            programmingProblem.setTemplates(JSONUtil.toJsonStr(request.getTemplates()));
            programmingProblem.setStandardSolution(JSONUtil.toJsonStr(request.getStandardSolution()));
            programmingProblem.setTimeLimit(request.getTimeLimit());
            programmingProblem.setMemoryLimit(request.getMemoryLimit());

            if (programmingProblemMapper.updateById(programmingProblem) <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新编程题详情失败");
            }

            return true;
        } catch (BusinessException e) {
            if (e.getCode() == ErrorCode.NOT_FOUND_ERROR.getCode()) {
                // 如果题目不存在，尝试添加新题目
                ProgrammingProblemAddRequest addRequest = new ProgrammingProblemAddRequest();
                BeanUtils.copyProperties(request, addRequest);
                addProgrammingProblem(addRequest, userId);
                return true;
            }
            throw e;
        }
    }
} 