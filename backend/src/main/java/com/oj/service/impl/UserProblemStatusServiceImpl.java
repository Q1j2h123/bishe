package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.mapper.ProblemMapper;
import com.oj.mapper.UserProblemStatusMapper;
import com.oj.model.entity.Problem;
import com.oj.model.entity.UserProblemStatus;
import com.oj.model.vo.UserProblemStatusVO;
import com.oj.service.UserProblemStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.oj.constant.SubmissionConstant.USER_STATUS_ATTEMPTED;
import static com.oj.constant.SubmissionConstant.USER_STATUS_SOLVED;

/**
 * 用户题目状态服务实现类
 */
@Service
@Slf4j
public class UserProblemStatusServiceImpl extends ServiceImpl<UserProblemStatusMapper, UserProblemStatus> implements UserProblemStatusService {
    
    @Resource
    private ProblemMapper problemMapper;
    
    @Override
    public UserProblemStatus getUserProblemStatus(Long userId, Long problemId) {
        LambdaQueryWrapper<UserProblemStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProblemStatus::getUserId, userId)
                   .eq(UserProblemStatus::getProblemId, problemId);
        
        return this.getOne(queryWrapper);
    }
    @Override
    public Map<Long, String> getBatchProblemStatus(Long userId, List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<UserProblemStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProblemStatus::getUserId, userId)
                .in(UserProblemStatus::getProblemId, problemIds);

        List<UserProblemStatus> statusList = this.list(queryWrapper);

        // 创建ID-状态映射
        Map<Long, String> statusMap = statusList.stream()
                .collect(Collectors.toMap(
                        UserProblemStatus::getProblemId,
                        UserProblemStatus::getStatus
                ));

        // 未解决的题目设为UNSOLVED
        problemIds.forEach(id -> statusMap.putIfAbsent(id, "UNSOLVED"));

        return statusMap;
    }
    @Override
    public List<UserProblemStatusVO> getUserSolvedProblems(Long userId) {
        // 查询用户已解决的题目
        LambdaQueryWrapper<UserProblemStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProblemStatus::getUserId, userId)
                   .eq(UserProblemStatus::getStatus, USER_STATUS_SOLVED);
        
        List<UserProblemStatus> statusList = this.list(queryWrapper);
        return convertToVOList(statusList);
    }
    
    @Override
    public List<UserProblemStatusVO> getUserAttemptedProblems(Long userId) {
        // 查询用户尝试过的题目
        LambdaQueryWrapper<UserProblemStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProblemStatus::getUserId, userId)
                   .eq(UserProblemStatus::getStatus, USER_STATUS_ATTEMPTED);
        
        List<UserProblemStatus> statusList = this.list(queryWrapper);
        return convertToVOList(statusList);
    }
    
    @Override
    public Long countUserSolvedProblems(Long userId) {
        LambdaQueryWrapper<UserProblemStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProblemStatus::getUserId, userId)
                   .eq(UserProblemStatus::getStatus, USER_STATUS_SOLVED);
        
        return this.count(queryWrapper);
    }
    
    @Override
    public Long countUserAttemptedProblems(Long userId) {
        LambdaQueryWrapper<UserProblemStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProblemStatus::getUserId, userId)
                   .eq(UserProblemStatus::getStatus, USER_STATUS_ATTEMPTED);
        
        return this.count(queryWrapper);
    }
    
    @Override
    public Page<UserProblemStatusVO> getUserProblemStatusList(Long userId, String status, long current, long size) {
        // 查询用户题目状态
        Page<UserProblemStatus> page = new Page<>(current, size);
        LambdaQueryWrapper<UserProblemStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProblemStatus::getUserId, userId);
        
        // 如果指定了状态，则按状态筛选
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(UserProblemStatus::getStatus, status);
        }
        
        // 按最后提交时间倒序排序
        queryWrapper.orderByDesc(UserProblemStatus::getLastSubmitTime);
        
        Page<UserProblemStatus> statusPage = this.page(page, queryWrapper);
        
        // 转换为VO
        Page<UserProblemStatusVO> resultPage = new Page<>(
                statusPage.getCurrent(),
                statusPage.getSize(),
                statusPage.getTotal());
        
        if (statusPage.getRecords().isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }
        
        List<UserProblemStatusVO> voList = convertToVOList(statusPage.getRecords());
        resultPage.setRecords(voList);
        
        return resultPage;
    }
    
    /**
     * 将UserProblemStatus列表转换为UserProblemStatusVO列表
     * @param statusList 状态列表
     * @return VO列表
     */
    private List<UserProblemStatusVO> convertToVOList(List<UserProblemStatus> statusList) {
        if (statusList == null || statusList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有题目ID
        List<Long> problemIds = statusList.stream()
                .map(UserProblemStatus::getProblemId)
                .collect(Collectors.toList());
        
        // 批量查询题目信息
        List<Problem> problems = problemMapper.selectBatchIds(problemIds);
        
        // 将题目信息映射为ID-Problem的形式，方便查找
        Map<Long, Problem> problemMap = problems.stream()
                .collect(Collectors.toMap(Problem::getId, problem -> problem));
        
        // 转换为VO列表
        return statusList.stream().map(status -> {
            UserProblemStatusVO vo = new UserProblemStatusVO();
            BeanUtils.copyProperties(status, vo);
            
            // 设置题目相关信息
            Problem problem = problemMap.get(status.getProblemId());
            if (problem != null) {
                vo.setProblemTitle(problem.getTitle());
                vo.setType(problem.getType());
                vo.setDifficulty(problem.getDifficulty());
                
                // 解析标签
                if (problem.getTags() != null && !problem.getTags().isEmpty()) {
                    vo.setTags(Arrays.asList(problem.getTags().split(",")));
                } else {
                    vo.setTags(new ArrayList<>());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
} 