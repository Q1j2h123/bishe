package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.common.ErrorCode;
import com.oj.exception.BusinessException;
import com.oj.mapper.QuestionBankMapper;
import com.oj.model.dto.QuestionBankAddRequest;
import com.oj.model.dto.QuestionBankQueryRequest;
import com.oj.model.entity.QuestionBank;
import com.oj.model.enums.JobTypeEnum;
import com.oj.service.QuestionBankService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 题库服务实现类
 */
@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Override
    public long addQuestionBank(QuestionBankAddRequest questionBankAddRequest) {
        // 参数校验
        if (questionBankAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建题库
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankAddRequest, questionBank);
        // 校验
        validQuestionBank(questionBank, true);
        // 保存题库
        boolean result = this.save(questionBank);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return questionBank.getId();
    }

    @Override
    public boolean deleteQuestionBank(long id) {
        // 参数校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 删除题库
        return this.removeById(id);
    }

    @Override
    public boolean updateQuestionBank(QuestionBank questionBank) {
        // 参数校验
        if (questionBank == null || questionBank.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验
        validQuestionBank(questionBank, false);
        // 更新题库
        return this.updateById(questionBank);
    }

    @Override
    public QuestionBank getQuestionBankById(long id) {
        // 参数校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取题库
        return this.getById(id);
    }

    @Override
    public Page<QuestionBank> listQuestionBankByPage(QuestionBankQueryRequest questionBankQueryRequest) {
        // 参数校验
        if (questionBankQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建分页对象
        long current = questionBankQueryRequest.getCurrent();
        long pageSize = questionBankQueryRequest.getPageSize();
        // 限制爬虫
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<QuestionBank> page = new Page<>(current, pageSize);
        // 构建查询条件
        QueryWrapper<QuestionBank> queryWrapper = getQueryWrapper(questionBankQueryRequest);
        // 执行查询
        return this.page(page, queryWrapper);
    }

    @Override
    public QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest questionBankQueryRequest) {
        QueryWrapper<QuestionBank> queryWrapper = new QueryWrapper<>();
        if (questionBankQueryRequest == null) {
            return queryWrapper;
        }
        // 题库名称
        String name = questionBankQueryRequest.getName();
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        // 岗位类型
        if (questionBankQueryRequest.getJobType() != null) {
            queryWrapper.eq("jobType", questionBankQueryRequest.getJobType().getValue());
        }
        // 难度等级
        if (questionBankQueryRequest.getLevel() != null) {
            queryWrapper.eq("level", questionBankQueryRequest.getLevel().getValue());
        }
        // 分类
        if (questionBankQueryRequest.getCategory() != null) {
            queryWrapper.eq("category", questionBankQueryRequest.getCategory().getValue());
        }
        // 题目类型
        if (questionBankQueryRequest.getType() != null) {
            queryWrapper.eq("type", questionBankQueryRequest.getType().getValue());
        }
        // 标签
        String tags = questionBankQueryRequest.getTags();
        if (StringUtils.isNotBlank(tags)) {
            queryWrapper.like("tags", tags);
        }
        // 排序
        String sortField = questionBankQueryRequest.getSortField();
        String sortOrder = questionBankQueryRequest.getSortOrder();
        if (StringUtils.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortField);
        } else {
            queryWrapper.orderByDesc("createTime");
        }
        return queryWrapper;
    }

    @Override
    public void validQuestionBank(QuestionBank questionBank, boolean add) {
        if (questionBank == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String name = questionBank.getName();
        JobTypeEnum jobType = questionBank.getJobType();

        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "题库名称不能为空");
            }
            if (jobType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "岗位类型不能为空");
            }
        }

        // 如果名称不为空，校验长度
        if (StringUtils.isNotBlank(name) && name.length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题库名称过长");
        }

        // 如果描述不为空，校验长度
        if (StringUtils.isNotBlank(questionBank.getDescription()) && questionBank.getDescription().length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题库描述过长");
        }
    }
} 