package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.common.ErrorCode;
import com.oj.exception.BusinessException;
import com.oj.mapper.QuestionBankProblemMapper;
import com.oj.model.entity.QuestionBankProblem;
import com.oj.service.QuestionBankProblemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 题库题目关联服务实现类
 */
@Service
public class QuestionBankProblemServiceImpl extends ServiceImpl<QuestionBankProblemMapper, QuestionBankProblem>
        implements QuestionBankProblemService {

    @Override
    public boolean addProblemToBank(long questionBankId, long problemId) {
        // 参数校验
        if (questionBankId <= 0 || problemId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查是否已存在
        QueryWrapper<QuestionBankProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionBankId", questionBankId)
                .eq("problemId", problemId);
        if (this.count(queryWrapper) > 0) {
            return true;
        }
        // 创建关联
        QuestionBankProblem questionBankProblem = new QuestionBankProblem();
        questionBankProblem.setQuestionBankId(questionBankId);
        questionBankProblem.setProblemId(problemId);
        return this.save(questionBankProblem);
    }

    @Override
    public boolean removeProblemFromBank(long questionBankId, long problemId) {
        // 参数校验
        if (questionBankId <= 0 || problemId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 删除关联
        QueryWrapper<QuestionBankProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionBankId", questionBankId)
                .eq("problemId", problemId);
        return this.remove(queryWrapper);
    }

    @Override
    public List<Long> getProblemIdsByBankId(long questionBankId) {
        // 参数校验
        if (questionBankId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询关联
        QueryWrapper<QuestionBankProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionBankId", questionBankId);
        List<QuestionBankProblem> questionBankProblems = this.list(queryWrapper);
        // 提取题目 id
        return questionBankProblems.stream()
                .map(QuestionBankProblem::getProblemId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean batchAddProblemsToBank(long questionBankId, List<Long> problemIds) {
        // 参数校验
        if (questionBankId <= 0 || problemIds == null || problemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 批量创建关联
        List<QuestionBankProblem> questionBankProblems = problemIds.stream()
                .map(problemId -> {
                    QuestionBankProblem questionBankProblem = new QuestionBankProblem();
                    questionBankProblem.setQuestionBankId(questionBankId);
                    questionBankProblem.setProblemId(problemId);
                    return questionBankProblem;
                })
                .collect(Collectors.toList());
        return this.saveBatch(questionBankProblems);
    }

    @Override
    public boolean batchRemoveProblemsFromBank(long questionBankId, List<Long> problemIds) {
        // 参数校验
        if (questionBankId <= 0 || problemIds == null || problemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 批量删除关联
        QueryWrapper<QuestionBankProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionBankId", questionBankId)
                .in("problemId", problemIds);
        return this.remove(queryWrapper);
    }
} 