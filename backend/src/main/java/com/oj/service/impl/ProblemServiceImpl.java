package com.oj.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.common.ErrorCode;
import com.oj.exception.BusinessException;
import com.oj.listener.ProblemImportListener;
import com.oj.mapper.ProblemMapper;
import com.oj.mapper.ChoiceProblemMapper;
import com.oj.mapper.JudgeProblemMapper;
import com.oj.mapper.ProgrammingProblemMapper;
import com.oj.model.dto.ProblemAddRequest;
import com.oj.model.dto.ProblemExcelDTO;
import com.oj.model.dto.ProblemQueryRequest;
import com.oj.model.dto.ProblemUpdateRequest;
import com.oj.model.entity.*;
import com.oj.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 问题服务实现类
 */
@Slf4j
@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {

    @Resource
    private ChoiceProblemMapper choiceProblemMapper;

    @Resource
    private JudgeProblemMapper judgeProblemMapper;

    @Resource
    private ProgrammingProblemMapper programmingProblemMapper;

    @Override
    public long addProblem(ProblemAddRequest problemAddRequest) {
        Problem problem = new Problem();
        BeanUtils.copyProperties(problemAddRequest, problem);
        // 设置初始值
        problem.setSubmissionCount(0);
        problem.setAcceptRate("0%");
        // 校验
        validProblem(problem, true);
        // 保存到数据库
        boolean result = this.save(problem);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return problem.getId();
    }

    @Override
    public boolean deleteProblem(long id) {
        return this.removeById(id);
    }

    @Override
    public boolean updateProblem(ProblemUpdateRequest problemUpdateRequest) {
        Problem problem = new Problem();
        BeanUtils.copyProperties(problemUpdateRequest, problem);
        // 校验
        validProblem(problem, false);
        return this.updateById(problem);
    }

    @Override
    public Problem getProblemById(long id) {
        return this.getById(id);
    }

    @Override
    public Page<Problem> listProblemByPage(ProblemQueryRequest problemQueryRequest) {
        long current = problemQueryRequest.getCurrent();
        long pageSize = problemQueryRequest.getPageSize();
        // 限制爬虫
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 执行查询
        return this.page(new Page<>(current, pageSize), getQueryWrapper(problemQueryRequest));
    }

    @Override
    public QueryWrapper<Problem> getQueryWrapper(ProblemQueryRequest problemQueryRequest) {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        if (problemQueryRequest == null) {
            return queryWrapper;
        }

        String searchText = problemQueryRequest.getSearchText();
        String type = problemQueryRequest.getType();
        String jobType = problemQueryRequest.getJobType();
        String tags = problemQueryRequest.getTags();
        String difficulty = problemQueryRequest.getDifficulty();
        String sortField = problemQueryRequest.getSortField();
        String sortOrder = problemQueryRequest.getSortOrder();

        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.like("title", searchText).or().like("content", searchText);
        }
        if (StringUtils.isNotBlank(type)) {
            queryWrapper.eq("type", type);
        }
        if (StringUtils.isNotBlank(jobType)) {
            queryWrapper.eq("jobType", jobType);
        }
        if (StringUtils.isNotBlank(tags)) {
            queryWrapper.like("tags", tags);
        }
        if (StringUtils.isNotBlank(difficulty)) {
            queryWrapper.eq("difficulty", difficulty);
        }

        // 排序
        if (StringUtils.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "asc".equals(sortOrder), sortField);
        }

        return queryWrapper;
    }

    @Override
    public void validProblem(Problem problem, boolean add) {
        if (problem == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String title = problem.getTitle();
        String content = problem.getContent();
        String type = problem.getType();
        String jobType = problem.getJobType();
        String tags = problem.getTags();
        String difficulty = problem.getDifficulty();

        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isBlank(title)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题不能为空");
            }
            if (StringUtils.isBlank(content)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容不能为空");
            }
            if (StringUtils.isBlank(type)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目类型不能为空");
            }
            if (StringUtils.isBlank(jobType)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "岗位类型不能为空");
            }
            if (StringUtils.isBlank(difficulty)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "难度不能为空");
            }
        }

        // 如果标题不为空，校验长度
        if (StringUtils.isNotBlank(title) && title.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }

        // 如果标签不为空，校验长度
        if (StringUtils.isNotBlank(tags) && tags.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签过长");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importProblems(List<ProblemExcelDTO> problemList) {
        if (problemList == null || problemList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目列表为空");
        }

        int successCount = 0;
        StringBuilder errorMsg = new StringBuilder();

        for (ProblemExcelDTO problemDTO : problemList) {
            try {
                createProblem(problemDTO);
                successCount++;
            } catch (Exception e) {
                errorMsg.append("题目[").append(problemDTO.getTitle()).append("]导入失败：")
                        .append(e.getMessage()).append("\n");
            }
        }

        return String.format("成功导入%d道题目。%s", successCount, 
                errorMsg.length() > 0 ? "\n失败原因：\n" + errorMsg : "");
    }

    @Override
    public void exportProblems(HttpServletResponse response) throws IOException {
        List<Problem> problems = this.list();
        List<ProblemExcelDTO> excelDTOs = new ArrayList<>();

        for (Problem problem : problems) {
            ProblemExcelDTO dto = new ProblemExcelDTO();
            // 设置基础信息
            dto.setTitle(problem.getTitle());
            dto.setContent(problem.getContent());
            dto.setType(problem.getType());
            dto.setJobType(problem.getJobType());
            dto.setTags(problem.getTags());
            dto.setDifficulty(problem.getDifficulty());

            // 根据题目类型设置特定信息
            switch (problem.getType()) {
                case "CHOICE":
                    ChoiceProblem choiceProblem = choiceProblemMapper.selectById(problem.getId());
                    if (choiceProblem != null) {
                        dto.setOptions(choiceProblem.getOptions());
                        dto.setAnswer(choiceProblem.getAnswer());
                        dto.setAnalysis(choiceProblem.getAnalysis());
                    }
                    break;
                case "JUDGE":
                    JudgeProblem judgeProblem = judgeProblemMapper.selectById(problem.getId());
                    if (judgeProblem != null) {
                        dto.setAnswer(judgeProblem.getAnswer().toString());
                        dto.setAnalysis(judgeProblem.getAnalysis());
                    }
                    break;
                case "PROGRAM":
                    ProgrammingProblem programmingProblem = programmingProblemMapper.selectById(problem.getId());
                    if (programmingProblem != null) {
                        dto.setTestCases(programmingProblem.getTestCases());
                        dto.setSampleInput(programmingProblem.getSampleInput());
                        dto.setSampleOutput(programmingProblem.getSampleOutput());
                        dto.setTimeLimit(programmingProblem.getTimeLimit());
                        dto.setMemoryLimit(programmingProblem.getMemoryLimit());
                        dto.setAnswer(programmingProblem.getTemplateCode());
                    }
                    break;
                default:
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的题目类型");
            }
            excelDTOs.add(dto);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("题目列表", String.valueOf(StandardCharsets.UTF_8));
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), ProblemExcelDTO.class)
                .sheet("题目列表")
                .doWrite(excelDTOs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProblem(ProblemExcelDTO problemDTO) {
        // 创建基础题目信息
        Problem problem = new Problem();
        problem.setTitle(problemDTO.getTitle());
        problem.setContent(problemDTO.getContent());
        problem.setType(problemDTO.getType());
        problem.setJobType(problemDTO.getJobType());
        problem.setTags(problemDTO.getTags());
        problem.setDifficulty(problemDTO.getDifficulty());
        
        // 保存基础信息
        if (!this.save(problem)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目保存失败");
        }

        // 根据题目类型保存特定信息
        switch (problemDTO.getType()) {
            case "CHOICE":
                ChoiceProblem choiceProblem = new ChoiceProblem();
                choiceProblem.setId(problem.getId());
                choiceProblem.setOptions(problemDTO.getOptions());
                choiceProblem.setAnswer(problemDTO.getAnswer());
                choiceProblem.setAnalysis(problemDTO.getAnalysis());
                choiceProblemMapper.insert(choiceProblem);
                break;
            case "JUDGE":
                JudgeProblem judgeProblem = new JudgeProblem();
                judgeProblem.setId(problem.getId());
                judgeProblem.setAnswer(Boolean.parseBoolean(problemDTO.getAnswer()));
                judgeProblem.setAnalysis(problemDTO.getAnalysis());
                judgeProblemMapper.insert(judgeProblem);
                break;
            case "PROGRAM":
                ProgrammingProblem programmingProblem = new ProgrammingProblem();
                programmingProblem.setId(problem.getId());
                programmingProblem.setTestCases(problemDTO.getTestCases());
                programmingProblem.setSampleInput(problemDTO.getSampleInput());
                programmingProblem.setSampleOutput(problemDTO.getSampleOutput());
                programmingProblem.setTimeLimit(problemDTO.getTimeLimit());
                programmingProblem.setMemoryLimit(problemDTO.getMemoryLimit());
                programmingProblem.setTemplateCode(problemDTO.getAnswer());
                programmingProblemMapper.insert(programmingProblem);
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的题目类型");
        }

        return problem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProblem(Long id, ProblemExcelDTO problemDTO) {
        Problem problem = this.getById(id);
        if (problem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        // 更新基础信息
        problem.setTitle(problemDTO.getTitle());
        problem.setContent(problemDTO.getContent());
        problem.setJobType(problemDTO.getJobType());
        problem.setTags(problemDTO.getTags());
        problem.setDifficulty(problemDTO.getDifficulty());
        
        if (!this.updateById(problem)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目更新失败");
        }

        // 根据题目类型更新特定信息
        switch (problemDTO.getType()) {
            case "CHOICE":
                ChoiceProblem choiceProblem = new ChoiceProblem();
                choiceProblem.setId(id);
                choiceProblem.setOptions(problemDTO.getOptions());
                choiceProblem.setAnswer(problemDTO.getAnswer());
                choiceProblem.setAnalysis(problemDTO.getAnalysis());
                choiceProblemMapper.updateById(choiceProblem);
                break;
            case "JUDGE":
                JudgeProblem judgeProblem = new JudgeProblem();
                judgeProblem.setId(id);
                judgeProblem.setAnswer(Boolean.parseBoolean(problemDTO.getAnswer()));
                judgeProblem.setAnalysis(problemDTO.getAnalysis());
                judgeProblemMapper.updateById(judgeProblem);
                break;
            case "PROGRAM":
                ProgrammingProblem programmingProblem = new ProgrammingProblem();
                programmingProblem.setId(id);
                programmingProblem.setTestCases(problemDTO.getTestCases());
                programmingProblem.setSampleInput(problemDTO.getSampleInput());
                programmingProblem.setSampleOutput(problemDTO.getSampleOutput());
                programmingProblem.setTimeLimit(problemDTO.getTimeLimit());
                programmingProblem.setMemoryLimit(problemDTO.getMemoryLimit());
                programmingProblem.setTemplateCode(problemDTO.getAnswer());
                programmingProblemMapper.updateById(programmingProblem);
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的题目类型");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProblem(Long id) {
        Problem problem = this.getById(id);
        if (problem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        // 删除基础信息
        if (!this.removeById(id)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目删除失败");
        }

        // 根据题目类型删除特定信息
        switch (problem.getType()) {
            case "CHOICE":
                choiceProblemMapper.deleteById(id);
                break;
            case "JUDGE":
                judgeProblemMapper.deleteById(id);
                break;
            case "PROGRAM":
                programmingProblemMapper.deleteById(id);
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的题目类型");
        }
    }

    @Override
    public ProblemExcelDTO getProblemDetail(Long id) {
        Problem problem = this.getById(id);
        if (problem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        ProblemExcelDTO dto = new ProblemExcelDTO();
        // 设置基础信息
        dto.setTitle(problem.getTitle());
        dto.setContent(problem.getContent());
        dto.setType(problem.getType());
        dto.setJobType(problem.getJobType());
        dto.setTags(problem.getTags());
        dto.setDifficulty(problem.getDifficulty());

        // 根据题目类型获取特定信息
        switch (problem.getType()) {
            case "CHOICE":
                ChoiceProblem choiceProblem = choiceProblemMapper.selectById(id);
                if (choiceProblem != null) {
                    dto.setOptions(choiceProblem.getOptions());
                    dto.setAnswer(choiceProblem.getAnswer());
                    dto.setAnalysis(choiceProblem.getAnalysis());
                }
                break;
            case "JUDGE":
                JudgeProblem judgeProblem = judgeProblemMapper.selectById(id);
                if (judgeProblem != null) {
                    dto.setAnswer(judgeProblem.getAnswer().toString());
                    dto.setAnalysis(judgeProblem.getAnalysis());
                }
                break;
            case "PROGRAM":
                ProgrammingProblem programmingProblem = programmingProblemMapper.selectById(id);
                if (programmingProblem != null) {
                    dto.setTestCases(programmingProblem.getTestCases());
                    dto.setSampleInput(programmingProblem.getSampleInput());
                    dto.setSampleOutput(programmingProblem.getSampleOutput());
                    dto.setTimeLimit(programmingProblem.getTimeLimit());
                    dto.setMemoryLimit(programmingProblem.getMemoryLimit());
                    dto.setAnswer(programmingProblem.getTemplateCode());
                }
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的题目类型");
        }

        return dto;
    }

    @Override
    public List<Problem> searchProblems(String title, String content, String type, String jobType,
                                      String difficulty, String tags, String sortField, String sortOrder) {
        // 构建查询条件
        LambdaQueryWrapper<Problem> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(Problem::getTitle, title);
        }
        if (StringUtils.isNotBlank(content)) {
            queryWrapper.like(Problem::getContent, content);
        }
        if (StringUtils.isNotBlank(type)) {
            queryWrapper.eq(Problem::getType, type);
        }
        if (StringUtils.isNotBlank(jobType)) {
            queryWrapper.eq(Problem::getJobType, jobType);
        }
        if (StringUtils.isNotBlank(difficulty)) {
            queryWrapper.eq(Problem::getDifficulty, difficulty);
        }
        if (StringUtils.isNotBlank(tags)) {
            queryWrapper.like(Problem::getTags, tags);
        }

        // 添加排序条件
        if (StringUtils.isNotBlank(sortField)) {
            // 根据sortOrder判断升序还是降序
            boolean isAsc = !"desc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "createTime":
                    queryWrapper.orderBy(true, isAsc, Problem::getCreateTime);
                    break;
                case "updateTime":
                    queryWrapper.orderBy(true, isAsc, Problem::getUpdateTime);
                    break;
                case "difficulty":
                    queryWrapper.orderBy(true, isAsc, Problem::getDifficulty);
                    break;
                default:
                    queryWrapper.orderBy(true, isAsc, Problem::getId);
                    break;
            }
        }

        // 执行查询
        return this.list(queryWrapper);
    }
} 