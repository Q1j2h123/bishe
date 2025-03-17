package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oj.mapper.ProblemMapper;
import com.oj.mapper.SubmissionMapper;
import com.oj.mapper.UserMapper;
import com.oj.model.entity.Problem;
import com.oj.model.entity.Submission;
import com.oj.model.entity.User;
import com.oj.model.vo.dashboard.*;
import com.oj.service.DashboardService;
import com.oj.exception.BusinessException;
import com.oj.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import com.oj.mapper.UserProblemStatusMapper;
import com.oj.model.entity.UserProblemStatus;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 控制面板服务实现类
 */
@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private ProblemMapper problemMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private SubmissionMapper submissionMapper;
    
    @Resource
    private UserProblemStatusMapper userProblemStatusMapper;

    @Resource
    private DataSource dataSource;

    /**
     * 获取控制面板统计数据
     */
    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO statsVO = new DashboardStatsVO();
        
        // 题目总数
        long problemCount = problemMapper.selectCount(null);
        statsVO.setProblemCount((int) problemCount);
        
        // 用户总数
        long userCount = userMapper.selectCount(null);
        statsVO.setUserCount((int) userCount);
        
        // 提交总数
        long submissionCount = submissionMapper.selectCount(null);
        statsVO.setSubmissionCount((int) submissionCount);
        
        // 通过率 - 使用user_problem_status表中的SOLVED状态  做的对题目数 / 提交的总次数
        QueryWrapper<UserProblemStatus> solvedWrapper = new QueryWrapper<>();
        solvedWrapper.eq("status", "SOLVED");
        long solvedCount = userProblemStatusMapper.selectCount(solvedWrapper);
        
        // 计算通过率 = 已解决的题目数 / 总提交次数
        double passRate = submissionCount > 0 ? (solvedCount * 100.0 / submissionCount) : 0;
        statsVO.setPassRate(Math.round(passRate * 10) / 10.0);
        
        // 今日活跃用户 - 使用提交记录来判断
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        
        // 查找今天有提交记录的用户数量
        QueryWrapper<Submission> todaySubmissionWrapper = new QueryWrapper<>();
        todaySubmissionWrapper.select("DISTINCT userId")
                            .ge("submissionTime", todayStart);
        
        // 由于没有last_login_time字段，改用今日提交统计作为活跃用户指标
        List<Object> todayActiveUserIds = submissionMapper.selectObjs(todaySubmissionWrapper);
        statsVO.setTodayActiveUsers(todayActiveUserIds.size());
        
        // 本周新增用户
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        
        QueryWrapper<User> weeklyNewWrapper = new QueryWrapper<>();
        weeklyNewWrapper.ge("createTime", weekStart);
        long weeklyNewUsers = userMapper.selectCount(weeklyNewWrapper);
        statsVO.setWeeklyNewUsers((int) weeklyNewUsers);
        
        // 题目分布
            DashboardStatsVO.ProblemDistribution distribution = new DashboardStatsVO.ProblemDistribution();
        
        QueryWrapper<Problem> choiceWrapper = new QueryWrapper<>();
        choiceWrapper.eq("type", "CHOICE");
        long choiceCount = problemMapper.selectCount(choiceWrapper);
        distribution.setChoice((int) choiceCount);
        
        QueryWrapper<Problem> judgeWrapper = new QueryWrapper<>();
        judgeWrapper.eq("type", "JUDGE");
        long judgeCount = problemMapper.selectCount(judgeWrapper);
        distribution.setJudge((int) judgeCount);
        
        QueryWrapper<Problem> programWrapper = new QueryWrapper<>();
        programWrapper.eq("type", "PROGRAM");
        long programCount = problemMapper.selectCount(programWrapper);
        distribution.setProgram((int) programCount);
        
            statsVO.setProblemDistribution(distribution);
            
        // 难度分布
            DashboardStatsVO.DifficultyDistribution difficultyDistribution = new DashboardStatsVO.DifficultyDistribution();
        
        QueryWrapper<Problem> easyWrapper = new QueryWrapper<>();
        easyWrapper.eq("difficulty", "EASY");
        long easyCount = problemMapper.selectCount(easyWrapper);
        difficultyDistribution.setEasy((int) easyCount);
        
        QueryWrapper<Problem> mediumWrapper = new QueryWrapper<>();
        mediumWrapper.eq("difficulty", "MEDIUM");
        long mediumCount = problemMapper.selectCount(mediumWrapper);
        difficultyDistribution.setMedium((int) mediumCount);
        
        QueryWrapper<Problem> hardWrapper = new QueryWrapper<>();
        hardWrapper.eq("difficulty", "HARD");
        long hardCount = problemMapper.selectCount(hardWrapper);
        difficultyDistribution.setHard((int) hardCount);
        
            statsVO.setDifficultyDistribution(difficultyDistribution);
        
        return statsVO;
    }

    /**
     * 获取最近活动记录
     */
    @Override
    public ActivityRecordVO[] getRecentActivities(Integer limit) {
        List<ActivityRecordVO> activities = new ArrayList<>();
        
        try {
            // 每种类型分配记录数量
            int recordsPerType = limit / 3;
            
            // 获取最近的提交记录
            QueryWrapper<Submission> submissionWrapper = new QueryWrapper<>();
            submissionWrapper.orderByDesc("submissionTime");
            submissionWrapper.last("LIMIT " + recordsPerType);
            List<Submission> submissions = submissionMapper.selectList(submissionWrapper);
            
            // 获取最近的题目操作记录（创建、更新）
            QueryWrapper<Problem> problemWrapper = new QueryWrapper<>();
            problemWrapper.orderByDesc("updateTime");
            problemWrapper.eq("isDelete", 0); // 只查询未删除的题目
            problemWrapper.last("LIMIT " + recordsPerType);
            List<Problem> problems = problemMapper.selectList(problemWrapper);
            
            // 获取最近删除的题目记录 - 使用直接的JDBC方式绕过MyBatis-Plus的逻辑删除过滤
            List<Problem> deletedProblems = new ArrayList<>();
            try {
                // 使用JdbcTemplate直接查询已删除的题目
                String sql = "SELECT id, title, type, userId, updateTime FROM problem WHERE isDelete = 1 ORDER BY updateTime DESC LIMIT " + recordsPerType;
                log.info("使用JdbcTemplate直接查询已删除的题目: {}", sql);
                
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
                log.info("JdbcTemplate查询结果数量: {}", maps != null ? maps.size() : 0);
                
                if (maps != null && !maps.isEmpty()) {
                    for (Map<String, Object> map : maps) {
                        Problem problem = new Problem();
                        problem.setId(getLongValue(map, "id"));
                        problem.setTitle((String) map.get("title"));
                        problem.setType((String) map.get("type"));
                        problem.setUserId(getLongValue(map, "userId"));
                        
                        // 安全处理日期类型转换
                        if (map.get("updateTime") != null) {
                            try {
                                // 根据实际数据库返回类型处理
                                if (map.get("updateTime") instanceof LocalDateTime) {
                                    problem.setUpdateTime((LocalDateTime) map.get("updateTime"));
                                } else if (map.get("updateTime") instanceof Date) {
                                    Date date = (Date) map.get("updateTime");
                                    problem.setUpdateTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
                                } else if (map.get("updateTime") instanceof java.sql.Timestamp) {
                                    java.sql.Timestamp timestamp = (java.sql.Timestamp) map.get("updateTime");
                                    problem.setUpdateTime(timestamp.toLocalDateTime());
                                } else {
                                    log.warn("未知的时间类型: {}", map.get("updateTime").getClass().getName());
                                }
                            } catch (Exception e) {
                                log.error("时间转换出错: {}", e.getMessage());
                                problem.setUpdateTime(LocalDateTime.now());
                            }
                        }
                        
                        deletedProblems.add(problem);
                    }
                    log.info("成功转换{}条已删除题目记录", deletedProblems.size());
                } else {
                    // 如果仍然没有找到记录，执行一个简单的count查询验证是否存在已删除记录
                    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM problem WHERE isDelete = 1", Integer.class);
                    log.warn("未找到已删除的题目记录，数据库中共有{}条已删除记录", count != null ? count : 0);
                }
            } catch (Exception e) {
                log.error("查询已删除题目时出错: {}", e.getMessage(), e);
            }
            
            log.info("查询活动记录 - 提交数量: {}, 未删除题目数量: {}, 已删除题目数量: {}", 
                     submissions != null ? submissions.size() : 0,
                     problems != null ? problems.size() : 0,
                     deletedProblems != null ? deletedProblems.size() : 0);
            
            // 将提交记录转换为活动记录
            if (submissions != null && !submissions.isEmpty()) {
                for (Submission submission : submissions) {
                    ActivityRecordVO activity = new ActivityRecordVO();
                    activity.setId(submission.getId());
                    activity.setUserId(submission.getUserId());
                    
                    // 获取用户名
                    try {
                        User user = userMapper.selectById(submission.getUserId());
                        activity.setUsername(user != null ? user.getUserName() : "未知用户");
                    } catch (Exception e) {
                        log.error("获取用户信息出错", e);
                        activity.setUsername("未知用户");
                    }
                    
                    activity.setAction("submit");
                    activity.setTargetType("题目");
                    activity.setTargetId(submission.getProblemId());
                    
                    // 获取题目名称
                    try {
                        Problem problem = problemMapper.selectById(submission.getProblemId());
                        activity.setTargetName(problem != null ? problem.getTitle() : "未知题目");
                    } catch (Exception e) {
                        log.error("获取题目信息出错", e);
                        activity.setTargetName("未知题目");
                    }
                    
                    // 将LocalDateTime转换为Date
                    if (submission.getSubmissionTime() != null) {
                        activity.setTime(Date.from(submission.getSubmissionTime().atZone(ZoneId.systemDefault()).toInstant()));
                    } else if (submission.getCreateTime() != null) {
                        activity.setTime(Date.from(submission.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
                    } else {
                        activity.setTime(new Date()); // 使用当前时间作为默认值
                    }
                    
                    activity.setIp("127.0.0.1"); // 实际应记录用户IP
                    
                    activities.add(activity);
                }
            }
            
            // 将题目操作记录转换为活动记录（添加/更新）
            if (problems != null && !problems.isEmpty()) {
                for (Problem problem : problems) {
                    ActivityRecordVO activity = new ActivityRecordVO();
                    activity.setId(problem.getId());
                    activity.setUserId(problem.getUserId());
                    
                    // 获取用户名（创建或更新题目的管理员）
                    try {
                        User user = userMapper.selectById(problem.getUserId());
                        activity.setUsername(user != null ? user.getUserName() : "未知用户");
                    } catch (Exception e) {
                        log.error("获取用户信息出错", e);
                        activity.setUsername("未知用户");
                    }
                    
                    // 判断活动类型（创建、更新）
                    LocalDateTime createTime = problem.getCreateTime();
                    LocalDateTime updateTime = problem.getUpdateTime();
                    
                    if (createTime != null && updateTime != null) {
                        // 如果创建时间和更新时间相差不到1分钟，视为创建操作
                        if (createTime.plusMinutes(1).isAfter(updateTime)) {
                            activity.setAction("add");
                        } else {
                            activity.setAction("update");
                        }
                    } else {
                        // 默认为更新
                        activity.setAction("update");
                    }
                    
                    activity.setTargetType("题目");
                    activity.setTargetId(problem.getId());
                    activity.setTargetName(problem.getTitle());
                    
                    // 使用更新时间作为活动时间
                    if (updateTime != null) {
                        activity.setTime(Date.from(updateTime.atZone(ZoneId.systemDefault()).toInstant()));
                    } else if (createTime != null) {
                        activity.setTime(Date.from(createTime.atZone(ZoneId.systemDefault()).toInstant()));
                    } else {
                        activity.setTime(new Date());
                    }
                    
                    activity.setIp("127.0.0.1");
                    activities.add(activity);
                }
            }
            
            // 将删除的题目记录转换为活动记录
            if (deletedProblems != null && !deletedProblems.isEmpty()) {
                log.info("发现{}条已删除题目记录", deletedProblems.size());
                for (Problem problem : deletedProblems) {
                    ActivityRecordVO activity = new ActivityRecordVO();
                    activity.setId(problem.getId());
                    activity.setUserId(problem.getUserId());
                    
                    // 获取用户名（删除题目的管理员）
                    try {
                        User user = userMapper.selectById(problem.getUserId());
                        activity.setUsername(user != null ? user.getUserName() : "未知用户");
                    } catch (Exception e) {
                        log.error("获取用户信息出错", e);
                        activity.setUsername("未知用户");
                    }
                    
                    // 设置为删除操作
                    activity.setAction("delete");
                    activity.setTargetType("题目");
                    activity.setTargetId(problem.getId());
                    activity.setTargetName(problem.getTitle());
                    
                    // 使用更新时间作为删除时间
                    LocalDateTime updateTime = problem.getUpdateTime();
                    if (updateTime != null) {
                        activity.setTime(Date.from(updateTime.atZone(ZoneId.systemDefault()).toInstant()));
                    } else {
                        activity.setTime(new Date()); // 默认当前时间
                    }
                    
                    activity.setIp("127.0.0.1");
                    activities.add(activity);
                }
            } else {
                log.warn("未找到已删除的题目记录");
            }
            
            // 按时间排序，最新的在前面
            activities.sort((a1, a2) -> a2.getTime().compareTo(a1.getTime()));
            
            // 如果超过限制数量，截取
            if (activities.size() > limit) {
                activities = activities.subList(0, limit);
            }
            
        } catch (Exception e) {
            log.error("获取最近活动记录出错", e);
        }
        
        return activities.toArray(new ActivityRecordVO[0]);
    }

    /**
     * 获取提交统计
     */
    @Override
    public SubmissionStatsVO getSubmissionStats() {
        SubmissionStatsVO statsVO = new SubmissionStatsVO();
        try {
            // 获取总提交数
            long totalSubmissions = submissionMapper.selectCount(null);
            statsVO.setTotalSubmissions((int) totalSubmissions);

            // 获取通过的提交数
            QueryWrapper<Submission> acceptedWrapper = new QueryWrapper<>();
            acceptedWrapper.eq("status", "ACCEPTED");
            long acceptedSubmissions = submissionMapper.selectCount(acceptedWrapper);
            statsVO.setAcceptedSubmissions((int) acceptedSubmissions);

            // 准备近7天的提交时间分布
            List<SubmissionStatsVO.TimeDistribution> timeDistribution = new ArrayList<>();
            LocalDate today = LocalDate.now();
            
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);

                QueryWrapper<Submission> dateWrapper = new QueryWrapper<>();
                dateWrapper.between("submissionTime", startOfDay, endOfDay);
                long count = submissionMapper.selectCount(dateWrapper);

                SubmissionStatsVO.TimeDistribution dto = new SubmissionStatsVO.TimeDistribution();
                dto.setDate(date.format(DateTimeFormatter.ofPattern("MM-dd")));
                dto.setCount((int) count);
                timeDistribution.add(dto);
            }
            statsVO.setTimeDistribution(timeDistribution);
            
            // 获取语言分布
            List<SubmissionStatsVO.LanguageDistribution> languageDistribution = new ArrayList<>();
            
            // 查询所有不同的语言类型
            QueryWrapper<Submission> languageQueryWrapper = new QueryWrapper<>();
            languageQueryWrapper.select("type", "COUNT(*) as count")
                                .groupBy("type");
            
            List<Map<String, Object>> languageCounts = submissionMapper.selectMaps(languageQueryWrapper);
            
            // 处理查询结果
            for (Map<String, Object> map : languageCounts) {
                String language = (String) map.get("type");
                Long count = ((Number) map.get("count")).longValue();
                
                if (language != null && !language.isEmpty()) {
                    SubmissionStatsVO.LanguageDistribution dto = new SubmissionStatsVO.LanguageDistribution();
                    dto.setLanguage(language);
                    dto.setCount(count.intValue());
                    languageDistribution.add(dto);
                }
            }
            
            statsVO.setLanguageDistribution(languageDistribution);
            
        } catch (Exception e) {
            log.error("获取提交统计出错", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取提交统计失败");
        }
        return statsVO;
    }

    // 辅助方法：安全地获取Long值
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (Exception e) {
            log.error("转换Long值出错: {}, 值: {}", e.getMessage(), value);
            return null;
        }
    }
} 