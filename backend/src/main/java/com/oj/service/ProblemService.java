package com.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.dto.ProblemDTO;
import com.oj.model.entity.Problem;
import com.oj.model.request.*;
import com.oj.model.vo.ProblemVO;

import java.util.List;

public interface ProblemService extends IService<Problem> {

    /**
     * 删除题目
     */
    boolean deleteProblem(Long id, Long userId);

    /**
     * 根据ID获取题目
     *
     * @param id 题目ID
     * @param userId 当前用户ID
     * @return 题目信息
     */
    ProblemVO getProblemById(Long id, Long userId);


    /**
     * 获取题目详细信息（包括具体题目内容）
     */
    ProblemDTO getProblemDetail(Long id, Long userId);

    Problem validateProblem(Long id, Long userId);



    /**
     * Problem 转 VO
     *
     * @param problem 题目实体
     * @param userId 当前用户ID
     * @return 题目VO
     */
    ProblemVO problemToVO(Problem problem, Long userId);

    /**
     * 获取题目的提交统计信息
     */
    void updateProblemSubmitInfo(Long problemId);

    /**
     * 批量获取题目
     *
     * @param problemIds 题目ID列表
     * @return 题目列表
     */
    List<ProblemVO> getProblemsByIds(List<Long> problemIds);

    /**
     * Problem 转 DTO
     *
     * @param problem 题目实体
     * @return 题目DTO
     */
    ProblemDTO problemToDTO(Problem problem);

    /**
     * DTO 转 VO
     *
     * @param problemDTO 题目DTO
     * @return 题目VO
     */
    ProblemVO dtoToVO(ProblemDTO problemDTO);

    /**
     * 获取用户的题目列表
     *
     * @param userId 用户ID
     * @return 题目列表
     */
    List<ProblemVO> getProblemsByUserId(Long userId);

    /**
     * 搜索题目
     *
     * @param keyword 关键词
     * @return 题目列表
     */
    List<ProblemVO> searchProblems(String keyword);

    /**
     * 添加选择题
     * @param request 选择题请求
     * @param userId 用户id
     * @return 题目id
     */
    Long addChoiceProblem(ChoiceProblemAddRequest request, Long userId);

    /**
     * 添加判断题
     * @param request 判断题请求
     * @param userId 用户id
     * @return 题目id
     */
    Long addJudgeProblem(JudgeProblemAddRequest request, Long userId);

    /**
     * 添加编程题
     * @param request 编程题请求
     * @param userId 用户id
     * @return 题目id
     */
    Long addProgramProblem(ProgramProblemAddRequest request, Long userId);

    /**
     * 更新选择题
     */
    boolean updateChoiceProblem(ChoiceProblemUpdateRequest request, Long userId);

    /**
     * 更新判断题
     */
    boolean updateJudgeProblem(JudgeProblemUpdateRequest request, Long userId);

    /**
     * 更新编程题
     */
    boolean updateProgramProblem(ProgramProblemUpdateRequest request, Long userId);

    /**
     * 分页获取题目列表
     * @param problemQueryRequest 查询条件
     * @param userId 当前用户ID
     * @return 分页结果
     */
    Page<ProblemVO> listProblemByPage(ProblemQueryRequest problemQueryRequest, Long userId);

    /**
     * 更新题目状态
     * @param problemId 题目ID
     * @param userId 用户ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateProblemStatus(Long problemId, Long userId, String status);

    /**
     * 获取随机一题
     * @param userId 用户ID
     * @return 随机题目
     */
    ProblemVO getRandomProblem(Long userId);

    /**
     * 获取每日一题
     * @param userId 用户ID
     * @return 每日题目
     */
    ProblemVO getDailyProblem(Long userId);

    /**
     * 高级搜索题目
     * @param problemSearchRequest 高级搜索请求
     * @param userId 当前用户ID
     * @return 分页结果
     */
    Page<ProblemVO> searchProblemAdvanced(ProblemSearchRequest problemSearchRequest, Long userId);
}