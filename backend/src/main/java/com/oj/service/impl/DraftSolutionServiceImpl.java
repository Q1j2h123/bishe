//package com.oj.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.oj.mapper.DraftSolutionMapper;
//import com.oj.model.entity.DraftSolution;
//import com.oj.model.request.DraftSolutionRequest;
//import com.oj.service.DraftSolutionService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
///**
// * 草稿解答服务实现类
// */
//@Service
//@Slf4j
//public class DraftSolutionServiceImpl extends ServiceImpl<DraftSolutionMapper, DraftSolution> implements DraftSolutionService {
//
//    @Override
//    public boolean saveDraft(Long userId, DraftSolutionRequest request) {
//        // 查询是否已有草稿
//        LambdaQueryWrapper<DraftSolution> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(DraftSolution::getUserId, userId)
//                   .eq(DraftSolution::getProblemId, request.getProblemId());
//
//        DraftSolution draftSolution = this.getOne(queryWrapper);
//
//        if (draftSolution == null) {
//            // 不存在则创建新草稿
//            draftSolution = new DraftSolution();
//            draftSolution.setUserId(userId);
//            draftSolution.setProblemId(request.getProblemId());
//            draftSolution.setType(request.getType());
//            draftSolution.setContent(request.getContent());
//            draftSolution.setLanguage(request.getLanguage());
//            draftSolution.setLastSaveTime(LocalDateTime.now());
//            draftSolution.setCreateTime(LocalDateTime.now());
//
//            return this.save(draftSolution);
//        } else {
//            // 存在则更新草稿
//            draftSolution.setContent(request.getContent());
//            draftSolution.setType(request.getType());
//            // 只有当请求中包含语言参数时才更新
//            if (request.getLanguage() != null) {
//                draftSolution.setLanguage(request.getLanguage());
//            }
//            draftSolution.setLastSaveTime(LocalDateTime.now());
//
//            return this.updateById(draftSolution);
//        }
//    }
//
//    @Override
//    public DraftSolution getUserDraft(Long userId, Long problemId) {
//        LambdaQueryWrapper<DraftSolution> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(DraftSolution::getUserId, userId)
//                   .eq(DraftSolution::getProblemId, problemId);
//
//        return this.getOne(queryWrapper);
//    }
//
//    @Override
//    public boolean deleteUserDraft(Long userId, Long problemId) {
//        LambdaQueryWrapper<DraftSolution> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(DraftSolution::getUserId, userId)
//                   .eq(DraftSolution::getProblemId, problemId);
//
//        return this.remove(queryWrapper);
//    }
//}