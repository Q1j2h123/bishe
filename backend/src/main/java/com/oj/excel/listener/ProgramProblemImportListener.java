package com.oj.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.oj.model.dto.TestCaseDTO;
import com.oj.model.excel.ProgramProblemExcel;
import com.oj.model.request.ProgramProblemAddRequest;
import com.oj.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 编程题导入监听器
 */
@Slf4j
public class ProgramProblemImportListener extends AnalysisEventListener<ProgramProblemExcel> {
    private final ProblemService problemService;
    private final Long userId;
    
    // 测试用例分隔符
    private static final String TEST_CASE_DELIMITER = "###";
    
    // 统计信息
    private int totalCount = 0;
    private int successCount = 0;
    private final List<String> errorMessages = new ArrayList<>();
    
    public ProgramProblemImportListener(ProblemService problemService, Long userId) {
        this.problemService = problemService;
        this.userId = userId;
    }
    
    @Override
    public void invoke(ProgramProblemExcel data, AnalysisContext context) {
        totalCount++;
        try {
            // 转换Excel数据为添加请求
            ProgramProblemAddRequest request = convertToAddRequest(data);
            // 调用添加方法
            problemService.addProgramProblem(request, userId);
            successCount++;
        } catch (Exception e) {
            log.error("导入编程题失败，题目标题: {}, 错误: {}", data.getTitle(), e.getMessage(), e);
            errorMessages.add("题目 [" + data.getTitle() + "] 导入失败: " + e.getMessage());
        }
    }
    
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("编程题导入完成，总数: {}, 成功: {}, 失败: {}", totalCount, successCount, totalCount - successCount);
    }
    
    /**
     * 将Excel数据转换为添加请求
     */
    private ProgramProblemAddRequest convertToAddRequest(ProgramProblemExcel data) {
        if (StringUtils.isBlank(data.getTitle())) {
            throw new IllegalArgumentException("题目标题不能为空");
        }
        if (StringUtils.isBlank(data.getContent())) {
            throw new IllegalArgumentException("题目内容不能为空");
        }
        if (StringUtils.isBlank(data.getFunctionName())) {
            throw new IllegalArgumentException("函数名称不能为空");
        }
        if (StringUtils.isBlank(data.getParamTypes())) {
            throw new IllegalArgumentException("参数类型不能为空");
        }
        if (StringUtils.isBlank(data.getReturnType())) {
            throw new IllegalArgumentException("返回值类型不能为空");
        }
        if (StringUtils.isBlank(data.getTestCaseInputs()) || StringUtils.isBlank(data.getTestCaseOutputs())) {
            throw new IllegalArgumentException("测试用例输入和输出不能为空");
        }
        if (StringUtils.isBlank(data.getJavaTemplate()) || StringUtils.isBlank(data.getJavaSolution())) {
            throw new IllegalArgumentException("Java代码模板和标准答案不能为空");
        }
        
        ProgramProblemAddRequest request = new ProgramProblemAddRequest();
        request.setTitle(data.getTitle());
        request.setContent(data.getContent());
        request.setType("PROGRAM");
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
        
        // 处理函数信息
        request.setFunctionName(data.getFunctionName());
        request.setParamTypes(Arrays.asList(data.getParamTypes().split(",")));
        request.setReturnType(data.getReturnType());
        
        // 处理测试用例
        List<TestCaseDTO> testCases = parseTestCases(data.getTestCaseInputs(), data.getTestCaseOutputs());
        request.setTestCases(testCases);
        
        // 处理代码模板
        Map<String, String> templates = new HashMap<>();
        templates.put("java", data.getJavaTemplate());
        
        if (StringUtils.isNotBlank(data.getPythonTemplate())) {
            templates.put("python", data.getPythonTemplate());
        }
        
        if (StringUtils.isNotBlank(data.getCppTemplate())) {
            templates.put("cpp", data.getCppTemplate());
        }
        
        request.setTemplates(templates);
        
        // 处理标准答案
        Map<String, String> standardSolution = new HashMap<>();
        standardSolution.put("java", data.getJavaSolution());
        request.setStandardSolution(standardSolution);
        
        // 处理时间和内存限制
        if (data.getTimeLimit() != null && data.getTimeLimit() > 0) {
            request.setTimeLimit(data.getTimeLimit());
        }
        
        if (data.getMemoryLimit() != null && data.getMemoryLimit() > 0) {
            request.setMemoryLimit(data.getMemoryLimit());
        }
        
        return request;
    }
    
    /**
     * 解析测试用例
     */
    private List<TestCaseDTO> parseTestCases(String inputs, String outputs) {
        // 拆分输入和输出
        String[] inputArray = inputs.split(TEST_CASE_DELIMITER);
        String[] outputArray = outputs.split(TEST_CASE_DELIMITER);
        
        // 检查输入和输出数量是否一致
        if (inputArray.length != outputArray.length) {
            throw new IllegalArgumentException("测试用例输入和输出数量不一致");
        }
        
        List<TestCaseDTO> testCases = new ArrayList<>();
        for (int i = 0; i < inputArray.length; i++) {
            TestCaseDTO testCase = new TestCaseDTO();
            testCase.setInput(inputArray[i].trim());
            testCase.setOutput(outputArray[i].trim());
            testCases.add(testCase);
        }
        
        return testCases;
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