//package com.oj.service;
//
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.oj.model.entity.DraftSolution;
//import com.oj.model.request.DraftSolutionRequest;
//
///**
// * 草稿解答服务接口
// */
//public interface DraftSolutionService extends IService<DraftSolution> {
//
//    /**
//     * 保存用户草稿
//     * @param userId 用户ID
//     * @param request 草稿请求
//     * @return 是否保存成功
//     */
//    boolean saveDraft(Long userId, DraftSolutionRequest request);
//
//    /**
//     * 获取用户题目草稿
//     * @param userId 用户ID
//     * @param problemId 题目ID
//     * @return 草稿实体，若不存在则返回null
//     */
//    DraftSolution getUserDraft(Long userId, Long problemId);
//
//    /**
//     * 删除用户题目草稿
//     * @param userId 用户ID
//     * @param problemId 题目ID
//     * @return 是否删除成功
//     */
//    boolean deleteUserDraft(Long userId, Long problemId);
//}