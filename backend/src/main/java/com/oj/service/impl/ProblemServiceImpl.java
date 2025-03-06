package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.common.ErrorCode;
import com.oj.constant.ProblemConstant;
import com.oj.exception.BusinessException;
import com.oj.mapper.*;
import com.oj.model.dto.ProblemDTO;
import com.oj.model.entity.*;
import com.oj.model.enums.ProblemTypeEnum;
import com.oj.model.enums.SubmissionStatusEnum;
import com.oj.model.request.ProblemAddRequest;
import com.oj.model.request.ProblemQueryRequest;
import com.oj.model.request.ProblemUpdateRequest;

import com.oj.model.vo.ProblemVO;
import com.oj.model.vo.UserVO;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProblemServiceImpl implements ProblemService {

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
    @Transactional(rollbackFor = Exception.class)
    public Long addProblem(ProblemAddRequest request, Long userId) {
        // 1. 参数校验
        ValidateUtils.validateProblemType(request.getType());
        ValidateUtils.validateDifficulty(request.getDifficulty());

        // 2. 创建题目基本信息
        Problem problem = new Problem();
        BeanUtils.copyProperties(request, problem);
        problem.setUserId(userId);
        problem.setAcceptRate("0%");
        problem.setSubmissionCount(0);
        problem.setSolutionCount(0);

        // 3. 处理标签
        if (StringUtils.isNotBlank(request.getTags())) {
            problem.setTags(formatTags(request.getTags()));
        }

        // 4. 保存题目基本信息
        if (problemMapper.insert(problem) <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目创建失败");
        }

        // 5. 保存具体题目内容
        try {
            saveProblemDetail(problem.getId(), request.getType(), (String) request.getProblemDetail());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目详情创建失败");
        }

        return problem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProblem(ProblemUpdateRequest request, Long userId) {
        // 1. 校验题目是否存在且有权限
        Problem oldProblem = validateProblem(request.getId(), userId);

        // 2. 校验参数
        ValidateUtils.validateProblemType(request.getType());
        ValidateUtils.validateDifficulty(request.getDifficulty());

        // 3. 更新基本信息
        Problem problem = new Problem();
        BeanUtils.copyProperties(request, problem);
        
        if (StringUtils.isNotBlank(request.getTags())) {
            problem.setTags(formatTags(request.getTags()));
        }

        // 4. 更新题目详情
        if (StringUtils.isNotBlank((CharSequence) request.getProblemDetail())) {
            try {
                updateProblemDetail(problem.getId(), request.getType(), (String) request.getProblemDetail());
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目详情更新失败");
            }
        }

        return problemMapper.updateById(problem) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProblem(Long id, Long userId) {
        // 校验题目是否存在且有权限
        Problem problem = validateProblem(id, userId);
        
        // 删除具体题目内容
        deleteProblemDetail(id, problem.getType());
        
        // 删除题目基本信息
        return problemMapper.deleteById(id) > 0;
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

        // 仅创建者和管理员可以操作
        if (!problem.getUserId().equals(userId)) {
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
            // 根据难度筛选
            if (StringUtils.isNotBlank(request.getDifficulty())) {
                queryWrapper.eq(Problem::getDifficulty, request.getDifficulty());
            }
            // 根据标签筛选
            if (StringUtils.isNotBlank(request.getTag())) {
                queryWrapper.like(Problem::getTags, request.getTag());
            }
            // 根据题目类型筛选
            if (StringUtils.isNotBlank(request.getType())) {
                queryWrapper.eq(Problem::getType, request.getType());
            }
            // 根据岗位类型筛选
            if (StringUtils.isNotBlank(request.getJobType())) {
                queryWrapper.eq(Problem::getJobType, request.getJobType());
            }
            // 创建者筛选
            if (request.getUserId() != null) {
                queryWrapper.eq(Problem::getUserId, request.getUserId());
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

        // 设置标签列表
        if (StringUtils.isNotBlank(problem.getTags())) {
            problemVO.setTags(Arrays.asList(problem.getTags().split(",")));
        }

        // 直接设置创建者的 userId
        problemVO.setUserId(problem.getUserId());

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
     * 删除具体题目内容
     */
    private void deleteProblemDetail(Long problemId, String type) {
        switch (ProblemTypeEnum.valueOf(type)) {
            case CHOICE:
                choiceProblemMapper.deleteById(problemId);
                break;
            case JUDGE:
                judgeProblemMapper.deleteById(problemId);
                break;
            case PROGRAM:
                programmingProblemMapper.deleteById(problemId);
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
    private String formatTags(String tags) {
        if (StringUtils.isBlank(tags)) {
            return "";
        }
        String[] tagArray = tags.split(",");
        Set<String> tagSet = Arrays.stream(tagArray)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        
        if (tagSet.size() > ProblemConstant.MAX_TAGS_COUNT) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签数量不能超过" + ProblemConstant.MAX_TAGS_COUNT);
        }
        
        return String.join(",", tagSet);
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
} 