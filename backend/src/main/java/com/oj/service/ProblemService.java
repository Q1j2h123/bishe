package com.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.model.dto.ProblemDTO;
import com.oj.model.entity.Problem;
import com.oj.model.request.ProblemAddRequest;
import com.oj.model.request.ProblemQueryRequest;
import com.oj.model.request.ProblemUpdateRequest;
import com.oj.model.vo.ProblemVO;

import java.util.List;

public interface ProblemService {
    /**
     * 创建题目
     */
    Long addProblem(ProblemAddRequest request, Long userId);

    /**
     * 删除题目
     */
    boolean deleteProblem(Long id, Long userId);

    /**
     * 更新题目
     */
    boolean updateProblem(ProblemUpdateRequest request, Long userId);

    /**
     * 根据ID获取题目
     */
    ProblemVO getProblemById(Long id, Long userId);

    /**
     * 分页获取题目列表
     */
    Page<ProblemVO> listProblem(ProblemQueryRequest request);

    /**
     * 获取题目详细信息（包括具体题目内容）
     */
    ProblemDTO getProblemDetail(Long id, Long userId);

    /**
     * 校验题目是否存在且是否有权限
     */
    Problem validateProblem(Long id, Long userId);

    ProblemVO problemToVO(Problem problem, Long userId);

    /**
     * 获取题目的提交统计信息
     */
    void updateProblemSubmitInfo(Long problemId);

    /**
     * 批量获取题目信息
     */
    List<ProblemVO> getProblemsByIds(List<Long> problemIds);

    /**
     * Entity 转 DTO
     */
    ProblemDTO problemToDTO(Problem problem);

    /**
     * DTO 转 VO
     */
    ProblemVO dtoToVO(ProblemDTO problemDTO);

    List<ProblemVO> getProblemsByUserId(Long userId);

    List<ProblemVO> searchProblems(String keyword);
}