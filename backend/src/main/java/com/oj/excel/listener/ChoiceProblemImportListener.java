package com.oj.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.oj.model.dto.ProblemOptionDTO;
import com.oj.model.excel.ChoiceProblemExcel;
import com.oj.model.request.ChoiceProblemAddRequest;
import com.oj.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 选择题导入监听器
 */
@Slf4j
public class ChoiceProblemImportListener extends AnalysisEventListener<ChoiceProblemExcel> {
    private final ProblemService problemService;
    private final Long userId;
    
    // 统计信息
    private int totalCount = 0;
    private int successCount = 0;
    private final List<String> errorMessages = new ArrayList<>();
    
    public ChoiceProblemImportListener(ProblemService problemService, Long userId) {
        this.problemService = problemService;
        this.userId = userId;
    }
    
    @Override
    public void invoke(ChoiceProblemExcel data, AnalysisContext context) {
        totalCount++;
        try {
            // 转换Excel数据为添加请求
            ChoiceProblemAddRequest request = convertToAddRequest(data);
            // 调用添加方法
            problemService.addChoiceProblem(request, userId);
            successCount++;
        } catch (Exception e) {
            log.error("导入选择题失败，题目标题: {}, 错误: {}", data.getTitle(), e.getMessage(), e);
            errorMessages.add("题目 [" + data.getTitle() + "] 导入失败: " + e.getMessage());
        }
    }
    
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("选择题导入完成，总数: {}, 成功: {}, 失败: {}", totalCount, successCount, totalCount - successCount);
    }
    
    /**
     * 将Excel数据转换为添加请求
     */
    private ChoiceProblemAddRequest convertToAddRequest(ChoiceProblemExcel data) {
        if (StringUtils.isBlank(data.getTitle())) {
            throw new IllegalArgumentException("题目标题不能为空");
        }
        if (StringUtils.isBlank(data.getContent())) {
            throw new IllegalArgumentException("题目内容不能为空");
        }
        if (StringUtils.isBlank(data.getOptionA()) || StringUtils.isBlank(data.getOptionB())) {
            throw new IllegalArgumentException("选项A和B不能为空");
        }
        if (StringUtils.isBlank(data.getAnswer())) {
            throw new IllegalArgumentException("正确答案不能为空");
        }
        
        ChoiceProblemAddRequest request = new ChoiceProblemAddRequest();
        request.setTitle(data.getTitle());
        request.setContent(data.getContent());
        request.setType("CHOICE");
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
        
        // 处理选项
        List<ProblemOptionDTO> options = new ArrayList<>();
        
        // 选项A
        ProblemOptionDTO optionA = new ProblemOptionDTO();
        optionA.setKey("A");
        optionA.setContent(data.getOptionA());
        options.add(optionA);
        
        // 选项B
        ProblemOptionDTO optionB = new ProblemOptionDTO();
        optionB.setKey("B");
        optionB.setContent(data.getOptionB());
        options.add(optionB);
        
        // 选项C（如果有）
        if (StringUtils.isNotBlank(data.getOptionC())) {
            ProblemOptionDTO optionC = new ProblemOptionDTO();
            optionC.setKey("C");
            optionC.setContent(data.getOptionC());
            options.add(optionC);
        }
        
        // 选项D（如果有）
        if (StringUtils.isNotBlank(data.getOptionD())) {
            ProblemOptionDTO optionD = new ProblemOptionDTO();
            optionD.setKey("D");
            optionD.setContent(data.getOptionD());
            options.add(optionD);
        }
        
        request.setOptions(options);
        request.setAnswer(data.getAnswer().trim().toUpperCase());
        request.setAnalysis(data.getAnalysis());
        
        return request;
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