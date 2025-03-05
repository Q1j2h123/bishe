package com.oj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.dto.QuestionBankAddRequest;
import com.oj.model.dto.QuestionBankQueryRequest;
import com.oj.model.entity.QuestionBank;

/**
 * 题库服务接口
 */
public interface QuestionBankService extends IService<QuestionBank> {

    /**
     * 添加题库
     *
     * @param questionBankAddRequest 题库添加请求
     * @return 题库 id
     */
    long addQuestionBank(QuestionBankAddRequest questionBankAddRequest);

    /**
     * 删除题库
     *
     * @param id 题库 id
     * @return 是否删除成功
     */
    boolean deleteQuestionBank(long id);

    /**
     * 更新题库
     *
     * @param questionBank 题库信息
     * @return 是否更新成功
     */
    boolean updateQuestionBank(QuestionBank questionBank);

    /**
     * 根据 id 获取题库
     *
     * @param id 题库 id
     * @return 题库信息
     */
    QuestionBank getQuestionBankById(long id);

    /**
     * 分页获取题库列表
     *
     * @param questionBankQueryRequest 查询条件
     * @return 题库列表
     */
    Page<QuestionBank> listQuestionBankByPage(QuestionBankQueryRequest questionBankQueryRequest);

    /**
     * 获取查询条件
     *
     * @param questionBankQueryRequest
     * @return
     */
    QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest questionBankQueryRequest);

    /**
     * 校验题库
     *
     * @param questionBank
     * @param add
     */
    void validQuestionBank(QuestionBank questionBank, boolean add);
} 