package com.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.dto.ErrorProblemDTO;
import com.oj.model.entity.ErrorProblem;
import com.oj.model.request.ErrorProblemQueryRequest;

/**
 * 错题本服务
 */
public interface ErrorProblemService extends IService<ErrorProblem> {

    /**
     * 添加错题记录
     * @param userId 用户ID
     * @param problemId 题目ID
     * @return 是否成功
     */
    boolean addErrorProblem(Long userId, Long problemId);

    /**
     * 更新错题记录错误时间
     * @param userId 用户ID
     * @param problemId 题目ID
     * @return 是否成功
     */
    boolean updateErrorTime(Long userId, Long problemId);

    /**
     * 标记题目为已掌握
     * @param userId 用户ID
     * @param problemId 题目ID
     * @return 是否成功
     */
    boolean markAsMastered(Long userId, Long problemId);

    /**
     * 分页获取用户错题记录
     * @param userId 用户ID
     * @param errorProblemQueryRequest 查询条件
     * @return 分页错题记录
     */
    Page<ErrorProblemDTO> getErrorProblemsByUserId(Long userId, ErrorProblemQueryRequest errorProblemQueryRequest);
} 