package com.oj.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.oj.model.excel.JudgeProblemExcel;
import com.oj.model.request.JudgeProblemAddRequest;
import com.oj.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判断题导入监听器
 */
@Slf4j
public class JudgeProblemImportListener extends AnalysisEventListener<JudgeProblemExcel> {
    private final ProblemService problemService;
    private final Long userId;
    
    // 统计信息
    private int totalCount = 0;
    private int successCount = 0;
    private final List<String> errorMessages = new ArrayList<>();
    
    public JudgeProblemImportListener(ProblemService problemService, Long userId) {
        this.problemService = problemService;
        this.userId = userId;
    }
    
    @Override
    public void invoke(JudgeProblemExcel data, AnalysisContext context) {
        totalCount++;
        try {
            // 转换Excel数据为添加请求
            JudgeProblemAddRequest request = convertToAddRequest(data);
            // 调用添加方法
            problemService.addJudgeProblem(request, userId);
            successCount++;
        } catch (Exception e) {
            log.error("导入判断题失败，题目标题: {}, 错误: {}", data.getTitle(), e.getMessage(), e);
            errorMessages.add("题目 [" + data.getTitle() + "] 导入失败: " + e.getMessage());
        }
    }
    
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("判断题导入完成，总数: {}, 成功: {}, 失败: {}", totalCount, successCount, totalCount - successCount);
    }
    
    /**
     * 将Excel数据转换为添加请求
     */
    private JudgeProblemAddRequest convertToAddRequest(JudgeProblemExcel data) {
        if (StringUtils.isBlank(data.getTitle())) {
            throw new IllegalArgumentException("题目标题不能为空");
        }
        if (StringUtils.isBlank(data.getContent())) {
            throw new IllegalArgumentException("题目内容不能为空");
        }
        if (StringUtils.isBlank(data.getAnswer())) {
            throw new IllegalArgumentException("正确答案不能为空");
        }
        
        JudgeProblemAddRequest request = new JudgeProblemAddRequest();
        request.setTitle(data.getTitle());
        request.setContent(data.getContent());
        request.setType("JUDGE");
        request.setDifficulty(convertDifficulty(data.getDifficulty()));
        request.setJobType(data.getJobType());
        
        // 处理标签
        if (StringUtils.isNotBlank(data.getTags())) {
            List<String> tags = Arrays.stream(data.getTags().split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            request.setTags(tags);
        }
        
        // 处理答案
        request.setAnswer(convertToBoolean(data.getAnswer()));
        request.setAnalysis(data.getAnalysis());
        
        return request;
    }
    
    /**
     * 转换答案为布尔值
     */
    private Boolean convertToBoolean(String answer) {
        if (StringUtils.isBlank(answer)) {
            throw new IllegalArgumentException("答案不能为空");
        }
        
        String trimmedAnswer = answer.trim().toLowerCase();
        if (trimmedAnswer.equals("true") || trimmedAnswer.equals("是") || trimmedAnswer.equals("正确") || trimmedAnswer.equals("t") || trimmedAnswer.equals("yes") || trimmedAnswer.equals("y")) {
            return true;
        } else if (trimmedAnswer.equals("false") || trimmedAnswer.equals("否") || trimmedAnswer.equals("错误") || trimmedAnswer.equals("f") || trimmedAnswer.equals("no") || trimmedAnswer.equals("n")) {
            return false;
        } else {
            throw new IllegalArgumentException("答案格式不正确，应为：true/false、是/否、正确/错误");
        }
    }
    
    /**
     * 转换难度
     */
    private String convertDifficulty(String difficulty) {
        if (StringUtils.isBlank(difficulty)) {
            return "MEDIUM"; // 默认中等难度
        }
        
        String upper = difficulty.trim().toUpperCase();
        if (upper.contains("简单") || upper.contains("EASY") || upper.equals("E")) {
            return "EASY";
        } else if (upper.contains("困难") || upper.contains("HARD") || upper.equals("H")) {
            return "HARD";
        } else {
            return "MEDIUM"; // 默认中等难度
        }
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public int getSuccessCount() {
        return successCount;
    }
    
    public List<String> getErrorMessages() {
        return errorMessages;
    }
} 