package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.mapper.ErrorProblemMapper;
import com.oj.mapper.ProblemMapper;
import com.oj.model.dto.ErrorProblemDTO;
import com.oj.model.entity.ErrorProblem;
import com.oj.model.entity.Problem;
import com.oj.model.request.ErrorProblemQueryRequest;
import com.oj.service.ErrorProblemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 错题本服务实现
 */
@Service
@Slf4j
public class ErrorProblemServiceImpl extends ServiceImpl<ErrorProblemMapper, ErrorProblem> implements ErrorProblemService {

    @Resource
    private ProblemMapper problemMapper;

    @Override
    public boolean addErrorProblem(Long userId, Long problemId) {
        // 查询是否已存在记录
        LambdaQueryWrapper<ErrorProblem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ErrorProblem::getUserId, userId)
                .eq(ErrorProblem::getProblemId, problemId);
        ErrorProblem errorProblem = this.getOne(queryWrapper);
        
        // 如果存在且是未掌握状态，则更新最近错误时间
        if (errorProblem != null) {
            if (errorProblem.getStatus() == 0) {
                return updateErrorTime(userId, problemId);
            } else {
                // 如果已经标记为掌握，则重新设为未掌握并更新时间
                errorProblem.setStatus(0);
                errorProblem.setLastErrorTime(LocalDateTime.now());
                return this.updateById(errorProblem);
            }
        }
        
        // 不存在则创建新记录
        ErrorProblem newErrorProblem = new ErrorProblem();
        newErrorProblem.setUserId(userId);
        newErrorProblem.setProblemId(problemId);
        newErrorProblem.setLastErrorTime(LocalDateTime.now());
        newErrorProblem.setStatus(0); // 0-未掌握
        return this.save(newErrorProblem);
    }

    @Override
    public boolean updateErrorTime(Long userId, Long problemId) {
        LambdaQueryWrapper<ErrorProblem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ErrorProblem::getUserId, userId)
                .eq(ErrorProblem::getProblemId, problemId);
        
        ErrorProblem errorProblem = this.getOne(queryWrapper);
        if (errorProblem == null) {
            return false;
        }
        
        errorProblem.setLastErrorTime(LocalDateTime.now());
        return this.updateById(errorProblem);
    }

    @Override
    public boolean markAsMastered(Long userId, Long problemId) {
        LambdaQueryWrapper<ErrorProblem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ErrorProblem::getUserId, userId)
                .eq(ErrorProblem::getProblemId, problemId);
        
        ErrorProblem errorProblem = this.getOne(queryWrapper);
        if (errorProblem == null) {
            return false;
        }
        
        errorProblem.setStatus(1); // 1-已掌握
        return this.updateById(errorProblem);
    }

    @Override
    public Page<ErrorProblemDTO> getErrorProblemsByUserId(Long userId, ErrorProblemQueryRequest errorProblemQueryRequest) {
        // 构建查询条件
        LambdaQueryWrapper<ErrorProblem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ErrorProblem::getUserId, userId)
                .eq(ErrorProblem::getStatus, 0) // 只查询未掌握的题目
                .orderByDesc(ErrorProblem::getLastErrorTime);
        
        // 分页查询错题记录
        Page<ErrorProblem> page = new Page<>(
                errorProblemQueryRequest.getCurrent(),
                errorProblemQueryRequest.getPageSize()
        );
        Page<ErrorProblem> errorProblemPage = this.page(page, queryWrapper);
        List<ErrorProblem> errorProblemList = errorProblemPage.getRecords();
        
        // 如果没有记录，直接返回空页
        if (errorProblemList.isEmpty()) {
            return new Page<ErrorProblemDTO>().setRecords(new ArrayList<>())
                    .setCurrent(errorProblemPage.getCurrent())
                    .setSize(errorProblemPage.getSize())
                    .setTotal(0);
        }
        
        // 提取题目ID列表
        List<Long> problemIds = errorProblemList.stream()
                .map(ErrorProblem::getProblemId)
                .collect(Collectors.toList());
        
        // 查询题目信息
        LambdaQueryWrapper<Problem> problemQueryWrapper = new LambdaQueryWrapper<>();
        problemQueryWrapper.in(Problem::getId, problemIds);
        
        // 添加条件过滤
        if (StringUtils.isNotBlank(errorProblemQueryRequest.getType())) {
            problemQueryWrapper.eq(Problem::getType, errorProblemQueryRequest.getType());
        }
        if (StringUtils.isNotBlank(errorProblemQueryRequest.getDifficulty())) {
            problemQueryWrapper.eq(Problem::getDifficulty, errorProblemQueryRequest.getDifficulty());
        }
        if (StringUtils.isNotBlank(errorProblemQueryRequest.getKeyword())) {
            problemQueryWrapper.like(Problem::getTitle, errorProblemQueryRequest.getKeyword());
        }
        
        List<Problem> problemList = problemMapper.selectList(problemQueryWrapper);
        
        // 合并错题记录和题目信息
        List<ErrorProblemDTO> errorProblemDTOList = new ArrayList<>();
        for (ErrorProblem errorProblem : errorProblemList) {
            for (Problem problem : problemList) {
                if (errorProblem.getProblemId().equals(problem.getId())) {
                    ErrorProblemDTO dto = new ErrorProblemDTO();
                    dto.setId(problem.getId());
                    dto.setTitle(problem.getTitle());
                    dto.setType(problem.getType());
                    dto.setDifficulty(problem.getDifficulty());
                    dto.setTags(problem.getTags());
                    dto.setLastErrorTime(errorProblem.getLastErrorTime());
                    errorProblemDTOList.add(dto);
                    break;
                }
            }
        }
        
        // 创建结果页
        Page<ErrorProblemDTO> resultPage = new Page<>();
        resultPage.setRecords(errorProblemDTOList);
        resultPage.setCurrent(errorProblemPage.getCurrent());
        resultPage.setSize(errorProblemPage.getSize());
        resultPage.setTotal(errorProblemDTOList.size());
        
        return resultPage;
    }
} 