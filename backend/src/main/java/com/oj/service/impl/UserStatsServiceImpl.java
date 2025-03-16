package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oj.mapper.ProblemMapper;
import com.oj.mapper.SubmissionMapper;
import com.oj.mapper.UserProblemStatusMapper;
import com.oj.model.dto.UserStatisticsDTO;
import com.oj.model.entity.Problem;
import com.oj.model.entity.Submission;
import com.oj.model.entity.UserProblemStatus;
import com.oj.service.UserStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户统计服务实现类
 */
@Service
@Slf4j
public class UserStatsServiceImpl implements UserStatsService {

    @Resource
    private SubmissionMapper submissionMapper;

    @Resource
    private ProblemMapper problemMapper;

    @Resource
    private UserProblemStatusMapper userProblemStatusMapper;

    @Override
    public UserStatisticsDTO getUserStatistics(Long userId) {
        UserStatisticsDTO statistics = new UserStatisticsDTO();
        
        // 查询用户提交记录
        LambdaQueryWrapper<Submission> submissionQuery = new LambdaQueryWrapper<>();
        submissionQuery.eq(Submission::getUserId, userId);
        long totalSubmissions = submissionMapper.selectCount(submissionQuery);
        statistics.setTotalSubmissions((int) totalSubmissions);
        
        // 查询用户已解决题目数量
        LambdaQueryWrapper<UserProblemStatus> statusQuery = new LambdaQueryWrapper<>();
        statusQuery.eq(UserProblemStatus::getUserId, userId)
                .eq(UserProblemStatus::getStatus, "SOLVED");
        long totalSolved = userProblemStatusMapper.selectCount(statusQuery);
        statistics.setTotalSolved((int) totalSolved);
        
        // 计算正确率
        if (totalSubmissions > 0) {
            double rate = (double) totalSolved / totalSubmissions * 100;
            statistics.setCorrectRate(String.format("%.1f%%", rate));
        } else {
            statistics.setCorrectRate("0.0%");
        }
        
        // 获取题目类型统计
        Map<String, UserStatisticsDTO.TypeCount> typeCounts = new HashMap<>();
        typeCounts.put("choice", getTypeCount(userId, "CHOICE"));
        typeCounts.put("judge", getTypeCount(userId, "JUDGE"));
        typeCounts.put("program", getTypeCount(userId, "PROGRAM"));
        statistics.setTypeCounts(typeCounts);
        
        // 获取题目难度统计
        Map<String, UserStatisticsDTO.TypeCount> difficultyCounts = new HashMap<>();
        difficultyCounts.put("easy", getDifficultyCount(userId, "EASY"));
        difficultyCounts.put("medium", getDifficultyCount(userId, "MEDIUM"));
        difficultyCounts.put("hard", getDifficultyCount(userId, "HARD"));
        statistics.setDifficultyCounts(difficultyCounts);
        
        return statistics;
    }
    
    /**
     * 获取题目类型统计
     */
    private UserStatisticsDTO.TypeCount getTypeCount(Long userId, String type) {
        UserStatisticsDTO.TypeCount typeCount = new UserStatisticsDTO.TypeCount();
        
        // 查询该类型的总题目数
        LambdaQueryWrapper<Problem> problemQuery = new LambdaQueryWrapper<>();
        problemQuery.eq(Problem::getType, type);
        long total = problemMapper.selectCount(problemQuery);
        typeCount.setTotal((int) total);
        
        // 查询用户已解决的该类型题目数
        LambdaQueryWrapper<Problem> solvedQuery = new LambdaQueryWrapper<>();
        solvedQuery.eq(Problem::getType, type);
        List<Problem> problems = problemMapper.selectList(solvedQuery);
        List<Long> problemIds = problems.stream().map(Problem::getId).collect(Collectors.toList());
        
        if (!problemIds.isEmpty()) {
            LambdaQueryWrapper<UserProblemStatus> statusQuery = new LambdaQueryWrapper<>();
            statusQuery.eq(UserProblemStatus::getUserId, userId)
                    .in(UserProblemStatus::getProblemId, problemIds)
                    .eq(UserProblemStatus::getStatus, "SOLVED");
            long solved = userProblemStatusMapper.selectCount(statusQuery);
            typeCount.setSolved((int) solved);
        } else {
            typeCount.setSolved(0);
        }
        
        return typeCount;
    }
    
    /**
     * 获取题目难度统计
     */
    private UserStatisticsDTO.TypeCount getDifficultyCount(Long userId, String difficulty) {
        UserStatisticsDTO.TypeCount difficultyCount = new UserStatisticsDTO.TypeCount();
        
        // 查询该难度的总题目数
        LambdaQueryWrapper<Problem> problemQuery = new LambdaQueryWrapper<>();
        problemQuery.eq(Problem::getDifficulty, difficulty);
        long total = problemMapper.selectCount(problemQuery);
        difficultyCount.setTotal((int) total);
        
        // 查询用户已解决的该难度题目数
        LambdaQueryWrapper<Problem> solvedQuery = new LambdaQueryWrapper<>();
        solvedQuery.eq(Problem::getDifficulty, difficulty);
        List<Problem> problems = problemMapper.selectList(solvedQuery);
        List<Long> problemIds = problems.stream().map(Problem::getId).collect(Collectors.toList());
        
        if (!problemIds.isEmpty()) {
            LambdaQueryWrapper<UserProblemStatus> statusQuery = new LambdaQueryWrapper<>();
            statusQuery.eq(UserProblemStatus::getUserId, userId)
                    .in(UserProblemStatus::getProblemId, problemIds)
                    .eq(UserProblemStatus::getStatus, "SOLVED");
            long solved = userProblemStatusMapper.selectCount(statusQuery);
            difficultyCount.setSolved((int) solved);
        } else {
            difficultyCount.setSolved(0);
        }
        
        return difficultyCount;
    }
} 