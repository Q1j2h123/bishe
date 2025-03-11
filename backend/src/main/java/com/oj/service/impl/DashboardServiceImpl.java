package com.oj.service.impl;

import com.oj.mapper.ProblemMapper;
import com.oj.mapper.SubmissionMapper;
import com.oj.mapper.UserMapper;
import com.oj.model.vo.dashboard.*;
import com.oj.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.text.SimpleDateFormat;
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

    /**
     * 获取控制面板统计数据
     */
    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO statsVO = new DashboardStatsVO();
        
        try {
            // 收集实际数据，如果数据库查询报错，使用默认值
            
            // 题目总数 - 默认使用模拟数据
            statsVO.setProblemCount(120);
            
            // 用户总数 - 默认使用模拟数据
            statsVO.setUserCount(580);
            
            // 提交总数 - 默认使用模拟数据
            statsVO.setSubmissionCount(3452);
            
            // 通过率 - 默认使用模拟数据
            statsVO.setPassRate(74.5);
            
            // 今日活跃用户 - 默认使用模拟数据
            statsVO.setTodayActiveUsers(42);
            
            // 本周新增用户 - 默认使用模拟数据
            statsVO.setWeeklyNewUsers(85);
            
            // 题目分布 - 默认使用模拟数据
            DashboardStatsVO.ProblemDistribution distribution = new DashboardStatsVO.ProblemDistribution();
            distribution.setChoice(45);
            distribution.setJudge(35);
            distribution.setProgram(40);
            statsVO.setProblemDistribution(distribution);
            
            // 难度分布 - 默认使用模拟数据
            DashboardStatsVO.DifficultyDistribution difficultyDistribution = new DashboardStatsVO.DifficultyDistribution();
            difficultyDistribution.setEasy(50);
            difficultyDistribution.setMedium(45);
            difficultyDistribution.setHard(25);
            statsVO.setDifficultyDistribution(difficultyDistribution);
            
            // TODO: 实际项目中，应该从数据库获取真实数据
            // Integer problemCount = problemMapper.countAllProblems();
            // statsVO.setProblemCount(problemCount);
            // 其他字段同理...
        } catch (Exception e) {
            log.error("获取控制面板统计数据出错", e);
            // 出错时返回默认值
        }
        
        return statsVO;
    }

    /**
     * 获取最近活动记录
     */
    @Override
    public ActivityRecordVO[] getRecentActivities(Integer limit) {
        // 模拟数据 - 实际项目中应该从数据库获取
        List<ActivityRecordVO> activities = new ArrayList<>();
        
        try {
            // TODO: 实际项目中，应该从数据库获取真实数据
            // List<ActivityEntity> recentActivities = activityMapper.getRecentActivities(limit);
            // activities = recentActivities.stream().map(this::convertToVO).collect(Collectors.toList());
            
            // 模拟数据
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            
            // 示例活动1
            ActivityRecordVO activity1 = new ActivityRecordVO();
            activity1.setId(1L);
            activity1.setUserId(1L);
            activity1.setUsername("张三");
            activity1.setAction("submit");
            activity1.setTargetType("题目");
            activity1.setTargetId(1L);
            activity1.setTargetName("两数之和");
            activity1.setTime(dateFormat.parse("2023-04-16T10:34:00"));
            activity1.setIp("192.168.1.1");
            activities.add(activity1);
            
            // 示例活动2
            ActivityRecordVO activity2 = new ActivityRecordVO();
            activity2.setId(2L);
            activity2.setUserId(100L);
            activity2.setUsername("管理员");
            activity2.setAction("add");
            activity2.setTargetType("题目");
            activity2.setTargetId(5L);
            activity2.setTargetName("合并K个排序链表");
            activity2.setTime(dateFormat.parse("2023-04-16T09:21:00"));
            activity2.setIp("192.168.1.100");
            activities.add(activity2);
            
            // 更多模拟活动...
            ActivityRecordVO activity3 = new ActivityRecordVO();
            activity3.setId(3L);
            activity3.setUserId(2L);
            activity3.setUsername("李四");
            activity3.setAction("register");
            activity3.setTargetType("系统");
            activity3.setTargetId(0L);
            activity3.setTargetName("用户注册");
            activity3.setTime(dateFormat.parse("2023-04-15T16:45:00"));
            activity3.setIp("192.168.1.2");
            activities.add(activity3);
            
            // 限制返回数量
            if (activities.size() > limit) {
                activities = activities.subList(0, limit);
            }
        } catch (Exception e) {
            log.error("获取最近活动记录出错", e);
            // 出错时返回空数组
        }
        
        return activities.toArray(new ActivityRecordVO[0]);
    }

    /**
     * 获取系统状态
     */
    @Override
    public SystemStatusVO getSystemStatus() {
        SystemStatusVO statusVO = new SystemStatusVO();
        
        try {
            // 获取系统信息
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            // CPU使用率 - 这是一个模拟值，实际获取CPU使用率需要更复杂的计算
            double cpuLoad = osBean.getSystemLoadAverage();
            double cpuUsage = cpuLoad > 0 ? Math.min(cpuLoad * 10, 100.0) : 45.0; // 默认45%
            statusVO.setCpuUsage(cpuUsage);
            
            // 内存使用率
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            double memoryUsage = maxMemory > 0 ? (usedMemory * 100.0 / maxMemory) : 62.0; // 默认62%
            statusVO.setMemoryUsage(memoryUsage);
            
            // 磁盘使用率 - 模拟值
            statusVO.setDiskUsage(38.0);
            
            // 平均响应时间 - 模拟值
            statusVO.setAverageResponseTime(125);
            
            // 在线用户数 - 模拟值，实际应该从在线用户会话统计
            statusVO.setOnlineUsers(48);
            
            // 判题服务器状态 - 模拟值，实际应该检测判题服务器是否正常
            statusVO.setJudgeServerStatus("up");
        } catch (Exception e) {
            log.error("获取系统状态出错", e);
            // 出错时使用默认值
            statusVO.setCpuUsage(45.0);
            statusVO.setMemoryUsage(62.0);
            statusVO.setDiskUsage(38.0);
            statusVO.setAverageResponseTime(125);
            statusVO.setOnlineUsers(48);
            statusVO.setJudgeServerStatus("up");
        }
        
        return statusVO;
    }

    /**
     * 获取提交统计
     */
    @Override
    public SubmissionStatsVO getSubmissionStats() {
        SubmissionStatsVO statsVO = new SubmissionStatsVO();
        
        try {
            // 总提交数 - 默认使用模拟数据
            statsVO.setTotalSubmissions(3452);
            
            // 通过的提交数 - 默认使用模拟数据
            statsVO.setAcceptedSubmissions(2570);
            
            // 最近7天的提交数据 - 默认使用模拟数据
            List<SubmissionStatsVO.TimeDistribution> timeDistribution = new ArrayList<>();
            
            // 生成最近7天的日期
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
            
            for (int i = 6; i >= 0; i--) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                String date = dateFormat.format(calendar.getTime());
                
                SubmissionStatsVO.TimeDistribution dayData = new SubmissionStatsVO.TimeDistribution();
                dayData.setDate(date);
                // 随机生成提交数 (实际项目中应该从数据库获取)
                dayData.setCount(new Random().nextInt(150) + 80);
                
                timeDistribution.add(dayData);
            }
            
            // 反转列表，使日期按照从早到晚排序
            Collections.reverse(timeDistribution);
            statsVO.setTimeDistribution(timeDistribution);
            
            // 语言分布 - 默认使用模拟数据
            List<SubmissionStatsVO.LanguageDistribution> languageDistribution = new ArrayList<>();
            
            SubmissionStatsVO.LanguageDistribution java = new SubmissionStatsVO.LanguageDistribution();
            java.setLanguage("Java");
            java.setCount(1200);
            languageDistribution.add(java);
            
            SubmissionStatsVO.LanguageDistribution python = new SubmissionStatsVO.LanguageDistribution();
            python.setLanguage("Python");
            python.setCount(980);
            languageDistribution.add(python);
            
            SubmissionStatsVO.LanguageDistribution cpp = new SubmissionStatsVO.LanguageDistribution();
            cpp.setLanguage("C++");
            cpp.setCount(750);
            languageDistribution.add(cpp);
            
            SubmissionStatsVO.LanguageDistribution javascript = new SubmissionStatsVO.LanguageDistribution();
            javascript.setLanguage("JavaScript");
            javascript.setCount(430);
            languageDistribution.add(javascript);
            
            SubmissionStatsVO.LanguageDistribution go = new SubmissionStatsVO.LanguageDistribution();
            go.setLanguage("Go");
            go.setCount(92);
            languageDistribution.add(go);
            
            statsVO.setLanguageDistribution(languageDistribution);
            
            // TODO: 实际项目中，应该从数据库获取真实数据
            // Integer totalSubmissions = submissionMapper.countAllSubmissions();
            // statsVO.setTotalSubmissions(totalSubmissions);
            // 其他字段同理...
        } catch (Exception e) {
            log.error("获取提交统计出错", e);
            // 出错时返回默认值的空对象
        }
        
        return statsVO;
    }
} 