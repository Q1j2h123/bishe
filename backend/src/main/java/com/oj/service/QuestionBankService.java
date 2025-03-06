package com.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.model.dto.QuestionBankDTO;
import com.oj.model.entity.QuestionBank;
import com.oj.model.request.QuestionBankAddRequest;
import com.oj.model.request.QuestionBankQueryRequest;
import com.oj.model.request.QuestionBankUpdateRequest;
import com.oj.model.vo.QuestionBankVO;

import java.util.List;

public interface QuestionBankService {
    /**
     * 创建题库
     */
    Long addQuestionBank(QuestionBankAddRequest request, Long userId);

    /**
     * 删除题库
     */
    boolean deleteQuestionBank(Long id, Long userId);

    /**
     * 更新题库
     */
    boolean updateQuestionBank(QuestionBankUpdateRequest request, Long userId);

    /**
     * 根据ID获取题库
     */
    QuestionBankVO getQuestionBankById(Long id, Long userId);

    /**
     * 根据ID获取题库详细信息
     */
    QuestionBankDTO getQuestionBankDetail(Long id, Long userId);

    /**
     * 分页获取题库列表
     */
    Page<QuestionBankVO> listQuestionBank(QuestionBankQueryRequest request);

    /**
     * 获取用户创建的题库列表
     */
    List<QuestionBankVO> getUserQuestionBanks(Long userId);

    /**
     * 向题库添加题目
     */
    boolean addProblemToBank(Long bankId, Long problemId, Long userId);

    /**
     * 从题库移除题目
     */
    boolean removeProblemFromBank(Long bankId, Long problemId, Long userId);

    /**
     * 获取题库中的所有题目ID
     */
    List<Long> getProblemIdsByBankId(Long bankId);

    /**
     * 校验题库是否存在且是否有权限
     */
    QuestionBank validateQuestionBank(Long id, Long userId);

    /**
     * 批量获取题库信息
     */
    List<QuestionBankVO> getQuestionBanksByIds(List<Long> bankIds);

    /**
     * Entity 转 DTO
     */
    QuestionBankDTO bankToDTO(QuestionBank questionBank);

    /**
     * DTO 转 VO
     */
    QuestionBankVO dtoToVO(QuestionBankDTO questionBankDTO);

    /**
     * 更新题库的题目数量
     */
    void updateProblemCount(Long bankId);

    List<QuestionBankVO> searchQuestionBanks(String keyword);

    List<QuestionBankVO> getHotQuestionBanks();
}