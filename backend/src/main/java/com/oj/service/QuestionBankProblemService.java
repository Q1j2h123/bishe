package com.oj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.entity.QuestionBankProblem;

import java.util.List;

/**
 * 题库题目关联服务接口
 */
public interface QuestionBankProblemService extends IService<QuestionBankProblem> {

    /**
     * 添加题目到题库
     *
     * @param questionBankId 题库 id
     * @param problemId 题目 id
     * @return 是否添加成功
     */
    boolean addProblemToBank(long questionBankId, long problemId);

    /**
     * 从题库中移除题目
     *
     * @param questionBankId 题库 id
     * @param problemId 题目 id
     * @return 是否移除成功
     */
    boolean removeProblemFromBank(long questionBankId, long problemId);

    /**
     * 获取题库中的所有题目 id
     *
     * @param questionBankId 题库 id
     * @return 题目 id 列表
     */
    List<Long> getProblemIdsByBankId(long questionBankId);

    /**
     * 批量添加题目到题库
     *
     * @param questionBankId 题库 id
     * @param problemIds 题目 id 列表
     * @return 是否添加成功
     */
    boolean batchAddProblemsToBank(long questionBankId, List<Long> problemIds);

    /**
     * 批量从题库中移除题目
     *
     * @param questionBankId 题库 id
     * @param problemIds 题目 id 列表
     * @return 是否移除成功
     */
    boolean batchRemoveProblemsFromBank(long questionBankId, List<Long> problemIds);
} 