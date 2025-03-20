package com.oj.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.oj.excel.listener.ChoiceProblemImportListener;
import com.oj.excel.listener.JudgeProblemImportListener;
import com.oj.excel.listener.ProgramProblemImportListener;
import com.oj.model.excel.ChoiceProblemExcel;
import com.oj.model.excel.JudgeProblemExcel;
import com.oj.model.excel.ProgramProblemExcel;
import com.oj.service.ImportService;
import com.oj.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入服务实现类
 */
@Service
@Slf4j
public class ImportServiceImpl implements ImportService {

    @Resource
    private ProblemService problemService;

    @Override
    public Map<String, Object> importChoiceProblems(MultipartFile file, Long userId) throws IOException {
        ChoiceProblemImportListener listener = new ChoiceProblemImportListener(problemService, userId);
        
        // 根据文件后缀确定Excel类型
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            log.error("文件名为空");
            throw new IOException("文件名为空");
        }
        
        try (BufferedInputStream bis = new BufferedInputStream(file.getInputStream())) {
            ExcelReader excelReader = EasyExcel.read(bis, ChoiceProblemExcel.class, listener)
                    .excelType(determineExcelType(fileName))
                    .autoCloseStream(false)  // 我们自己关闭流
                    .build();
            
            excelReader.readAll();
            excelReader.finish();
        } catch (Exception e) {
            log.error("导入选择题出错", e);
            throw new IOException("导入失败: " + e.getMessage(), e);
        }
        
        return buildImportResult(listener.getTotalCount(), listener.getSuccessCount(), listener.getErrorMessages());
    }

    @Override
    public Map<String, Object> importJudgeProblems(MultipartFile file, Long userId) throws IOException {
        JudgeProblemImportListener listener = new JudgeProblemImportListener(problemService, userId);
        
        // 根据文件后缀确定Excel类型
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            log.error("文件名为空");
            throw new IOException("文件名为空");
        }
        
        try (BufferedInputStream bis = new BufferedInputStream(file.getInputStream())) {
            ExcelReader excelReader = EasyExcel.read(bis, JudgeProblemExcel.class, listener)
                    .excelType(determineExcelType(fileName))
                    .autoCloseStream(false)  // 我们自己关闭流
                    .build();
            
            excelReader.readAll();
            excelReader.finish();
        } catch (Exception e) {
            log.error("导入判断题出错", e);
            throw new IOException("导入失败: " + e.getMessage(), e);
        }
        
        return buildImportResult(listener.getTotalCount(), listener.getSuccessCount(), listener.getErrorMessages());
    }

    @Override
    public Map<String, Object> importProgramProblems(MultipartFile file, Long userId) throws IOException {
        ProgramProblemImportListener listener = new ProgramProblemImportListener(problemService, userId);
        
        // 根据文件后缀确定Excel类型
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            log.error("文件名为空");
            throw new IOException("文件名为空");
        }
        
        // 检查是否为CSV文件，如果是则使用特殊处理
        if (fileName.toLowerCase().endsWith(".csv")) {
            return importProgramProblemsFromCSV(file, userId);
        }
        
        try (BufferedInputStream bis = new BufferedInputStream(file.getInputStream())) {
            // 使用xlsx格式处理文件，以确保正确处理代码块
            ExcelReader excelReader = EasyExcel.read(bis, ProgramProblemExcel.class, listener)
                    .excelType(determineExcelType(fileName))
                    .autoCloseStream(false)  // 我们自己关闭流
                    .build();
            
            excelReader.readAll();
            excelReader.finish();
        } catch (Exception e) {
            log.error("导入编程题出错", e);
            throw new IOException("导入失败: " + e.getMessage(), e);
        }
        
        return buildImportResult(listener.getTotalCount(), listener.getSuccessCount(), listener.getErrorMessages());
    }
    
    /**
     * 从CSV文件导入编程题（特殊处理）
     */
    private Map<String, Object> importProgramProblemsFromCSV(MultipartFile file, Long userId) throws IOException {
        log.info("使用自定义CSV解析导入编程题");
        int totalCount = 0;
        int successCount = 0;
        List<String> errorMessages = new ArrayList<>();
        
        // 尝试多种字符编码
        List<String> encodingsToTry = new ArrayList<>();
        encodingsToTry.add(StandardCharsets.UTF_8.name());
        encodingsToTry.add("GBK");
        encodingsToTry.add("GB2312");
        encodingsToTry.add("UTF-16LE");
        encodingsToTry.add("ISO-8859-1");
        
        Exception lastException = null;
        
        // 依次尝试不同编码
        for (String encoding : encodingsToTry) {
            // 使用ByteArrayInputStream存储文件内容以支持多次读取
            // 首先将整个文件读入字节数组
            byte[] fileBytes;
            try {
                fileBytes = file.getBytes();
            } catch (IOException e) {
                log.error("读取文件内容失败", e);
                throw new IOException("读取文件内容失败: " + e.getMessage(), e);
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new java.io.ByteArrayInputStream(fileBytes)), encoding))) {
                // 检测并跳过BOM标记
                reader.mark(4);
                if (reader.read() == 0xFEFF) {
                    // 发现BOM标记，继续读取
                    log.info("检测到UTF-8 BOM标记，已跳过");
                } else {
                    // 没有BOM标记，重置流位置
                    reader.reset();
                }
                
                log.info("尝试使用{}编码解析CSV文件", encoding);
                
                // 使用更宽松的CSV配置
                CSVFormat csvFormat = CSVFormat.DEFAULT
                        .withHeader()              // 使用第一行作为标题
                        .withSkipHeaderRecord(true) // 跳过标题行
                        .withIgnoreEmptyLines(true)
                        .withTrim(true)            // 忽略周围空格
                        .withEscape('\\')          // 设置转义字符
                        .withQuote('"')            // 设置引号字符
                        .withAllowMissingColumnNames(true);
                
                // 检测是否包含有效中文
                boolean hasValidChineseChars = false;
                try (CSVParser csvParser = new CSVParser(reader, csvFormat)) {
                    // 先进行一次检测，确认编码是否能正确解析中文
                    for (CSVRecord record : csvParser) {
                        if (record.size() > 0) {
                            String title = record.get(0);
                            // 检查是否包含常见中文字符
                            if (title != null && title.matches(".*[\\u4e00-\\u9fa5]+.*")) {
                                log.info("检测到有效中文: {}", title);
                                hasValidChineseChars = true;
                                break;
                            }
                        }
                    }
                }
                
                if (!hasValidChineseChars) {
                    log.warn("使用{}编码未检测到有效中文字符，尝试下一种编码", encoding);
                    continue;
                }
                
                // 重新创建一个新的Reader来解析文件内容
                try (BufferedReader reReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new java.io.ByteArrayInputStream(fileBytes)), encoding))) {
                    // 再次检测并跳过BOM标记
                    reReader.mark(4);
                    if (reReader.read() == 0xFEFF) {
                        // 跳过BOM
                    } else {
                        reReader.reset();
                    }
                    
                    try (CSVParser csvParser = new CSVParser(reReader, csvFormat)) {
                        for (CSVRecord record : csvParser) {
                            totalCount++;
                            try {
                                // 构建编程题对象
                                ProgramProblemExcel programProblem = new ProgramProblemExcel();
                                
                                // 设置必要字段，调整索引以匹配您的CSV格式
                                if (record.size() >= 16) { // 确保有足够的列
                                    // 应用修复编码的方法处理每个字段
                                    programProblem.setTitle(sanitizeString(record.get(0)));
                                    programProblem.setContent(sanitizeString(record.get(1)));
                                    programProblem.setFunctionName(sanitizeString(record.get(2)));
                                    programProblem.setParamTypes(sanitizeString(record.get(3)));
                                    programProblem.setReturnType(sanitizeString(record.get(4)));
                                    programProblem.setTestCaseInputs(sanitizeString(record.get(5)));
                                    programProblem.setTestCaseOutputs(sanitizeString(record.get(6)));
                                    programProblem.setJavaTemplate(sanitizeString(record.get(7)));
                                    programProblem.setPythonTemplate(sanitizeString(record.get(8)));
                                    programProblem.setCppTemplate(sanitizeString(record.get(9)));
                                    // C语言模板在第10列，但实体类中没有对应字段，跳过
                                    if (record.size() > 10) programProblem.setCTemplate(sanitizeString(record.get(10)));
                                    
                                    // 设置各语言的标准答案
                                    if (record.size() > 11) programProblem.setJavaSolution(sanitizeString(record.get(11)));
                                    if (record.size() > 12) programProblem.setPythonSolution(sanitizeString(record.get(12)));
                                    if (record.size() > 13) programProblem.setCppSolution(sanitizeString(record.get(13)));
                                    if (record.size() > 14) programProblem.setCSolution(sanitizeString(record.get(14)));
                                    
                                    // 处理可选字段
                                    try {
                                        if (record.size() > 15 && record.get(15) != null && !record.get(15).isEmpty()) {
                                            programProblem.setTimeLimit(Integer.parseInt(sanitizeString(record.get(15))));
                                        }
                                    } catch (NumberFormatException e) {
                                        log.warn("解析时间限制失败，使用默认值: {}", e.getMessage());
                                    }
                                    
                                    try {
                                        if (record.size() > 16 && record.get(16) != null && !record.get(16).isEmpty()) {
                                            programProblem.setMemoryLimit(Integer.parseInt(sanitizeString(record.get(16))));
                                        }
                                    } catch (NumberFormatException e) {
                                        log.warn("解析内存限制失败，使用默认值: {}", e.getMessage());
                                    }
                                    
                                    if (record.size() > 17) {
                                        // 确保难度值是有效的枚举值
                                        String difficulty = sanitizeString(record.get(17));
                                        if (difficulty != null) {
                                            difficulty = difficulty.trim().toUpperCase();
                                            if (difficulty.equals("简单") || difficulty.equals("SIMPLE")) {
                                                difficulty = "EASY";
                                            } else if (difficulty.equals("中等") || difficulty.equals("MEDIUM")) {
                                                difficulty = "MEDIUM";
                                            } else if (difficulty.equals("困难") || difficulty.equals("HARD")) {
                                                difficulty = "HARD";
                                            }
                                            programProblem.setDifficulty(difficulty);
                                        }
                                    }
                                    
                                    if (record.size() > 18) {
                                        // 确保jobType值是有效的，不包含长代码块
                                        String jobType = sanitizeString(record.get(18));
                                        if (jobType != null) {
                                            jobType = jobType.trim();
                                            // 如果jobType超过50个字符，可能是错误数据
                                            if (jobType.length() > 50) {
                                                jobType = "ALGORITHM"; // 使用默认值
                                            }
                                            programProblem.setJobType(jobType);
                                        }
                                    }
                                    
                                    if (record.size() > 19) programProblem.setTags(sanitizeString(record.get(19)));
                                    
                                    log.info("处理后的标题: {}", programProblem.getTitle());
                                    
                                    // 将编程题添加到数据库
                                    problemService.addProgramProblem(convertToProgramProblemAddRequest(programProblem), userId);
                                    successCount++;
                                } else {
                                    throw new IllegalArgumentException("CSV记录列数不足，至少需要16列");
                                }
                            } catch (Exception e) {
                                log.error("导入第{}条记录失败: {}", totalCount, e.getMessage(), e);
                                errorMessages.add("第" + totalCount + "条记录导入失败: " + e.getMessage());
                            }
                        }
                        
                        // 如果能成功解析，则退出编码尝试循环
                        log.info("成功使用{}编码解析CSV文件", encoding);
                        return buildImportResult(totalCount, successCount, errorMessages);
                    }
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("使用{}编码解析CSV文件失败: {}", encoding, e.getMessage());
                // 重置计数器，准备尝试下一种编码
                totalCount = 0;
                successCount = 0;
                errorMessages.clear();
            }
        }
        
        // 所有编码都尝试失败
        log.error("所有编码尝试均失败，无法解析CSV文件", lastException);
        throw new IOException("CSV文件解析失败: " + (lastException != null ? lastException.getMessage() : "未知错误"), lastException);
    }
    
    /**
     * 清理和修复字符串编码问题
     */
    private String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        
        // 如果字符串看起来像乱码，尝试不同的编码转换
        if (containsGarbledChars(input)) {
            try {
                // 尝试通过ISO-8859-1->UTF-8的重编码修复
                byte[] bytes = input.getBytes("ISO-8859-1");
                String utf8String = new String(bytes, StandardCharsets.UTF_8);
                
                // 如果转换后的结果看起来合理（包含中文字符），则使用它
                if (utf8String.matches(".*[\\u4e00-\\u9fa5]+.*")) {
                    return utf8String;
                }
                
                // 尝试GBK
                String gbkString = new String(bytes, "GBK");
                if (gbkString.matches(".*[\\u4e00-\\u9fa5]+.*")) {
                    return gbkString;
                }
            } catch (Exception e) {
                log.warn("字符编码转换失败: {}", e.getMessage());
            }
        }
        
        // 默认返回原始输入
        return input;
    }
    
    /**
     * 检查字符串是否包含可能是乱码的字符
     */
    private boolean containsGarbledChars(String str) {
        // 检查是否包含常见的乱码字符或模式
        return str.contains("") || 
               str.contains("锟") || 
               str.contains("鈥") ||
               // 检查是否是纯ASCII但应该有中文的情况
               (str.matches("^\\p{ASCII}*$") && 
                (str.contains("zhong") || str.contains("wen") || 
                 str.contains("han") || str.contains("zi")));
    }
    
    /**
     * 将Excel对象转换为添加请求对象
     */
    private com.oj.model.request.ProgramProblemAddRequest convertToProgramProblemAddRequest(ProgramProblemExcel excel) {
        com.oj.model.request.ProgramProblemAddRequest request = new com.oj.model.request.ProgramProblemAddRequest();
        
        // 设置基本字段
        request.setTitle(excel.getTitle());
        request.setContent(excel.getContent());
        request.setFunctionName(excel.getFunctionName());
        
        // 处理参数类型
        if (excel.getParamTypes() != null && !excel.getParamTypes().isEmpty()) {
            List<String> paramTypesList = new ArrayList<>();
            if (excel.getParamTypes().contains(",")) {
                // 如果有逗号分隔，按逗号分割
                String[] types = excel.getParamTypes().split(",");
                for (String type : types) {
                    paramTypesList.add(type.trim());
                }
            } else {
                // 否则作为单个参数类型
                paramTypesList.add(excel.getParamTypes().trim());
            }
            request.setParamTypes(paramTypesList);
        } else {
            request.setParamTypes(new ArrayList<>());
        }
        
        request.setReturnType(excel.getReturnType());
        
        // 处理测试用例
        if (excel.getTestCaseInputs() != null && excel.getTestCaseOutputs() != null) {
            List<com.oj.model.dto.TestCaseDTO> testCases = new ArrayList<>();
            String[] inputs = excel.getTestCaseInputs().split("###");
            String[] outputs = excel.getTestCaseOutputs().split("###");
            
            int count = Math.min(inputs.length, outputs.length);
            for (int i = 0; i < count; i++) {
                com.oj.model.dto.TestCaseDTO testCase = new com.oj.model.dto.TestCaseDTO();
                testCase.setInput(inputs[i].trim());
                testCase.setOutput(outputs[i].trim());
                testCases.add(testCase);
            }
            
            request.setTestCases(testCases);
        }
        
        // 处理代码模板
        Map<String, String> templates = new HashMap<>();
        if (excel.getJavaTemplate() != null && !excel.getJavaTemplate().isEmpty()) {
            templates.put("java", excel.getJavaTemplate());
        }
        
        if (excel.getPythonTemplate() != null && !excel.getPythonTemplate().isEmpty()) {
            templates.put("python", excel.getPythonTemplate());
        }
        
        if (excel.getCppTemplate() != null && !excel.getCppTemplate().isEmpty()) {
            templates.put("cpp", excel.getCppTemplate());
        }
        
        // 添加对C语言模板的支持
        if (excel.getCTemplate() != null && !excel.getCTemplate().isEmpty()) {
            templates.put("c", excel.getCTemplate());
        }
        
        request.setTemplates(templates);
        
        // 处理标准答案 - 修改为支持所有四种语言
        Map<String, String> standardSolution = new HashMap<>();
        if (excel.getJavaSolution() != null && !excel.getJavaSolution().isEmpty()) {
            standardSolution.put("java", excel.getJavaSolution());
        }
        
        // 添加对Python、C++和C语言标准答案的支持
        if (excel.getPythonSolution() != null && !excel.getPythonSolution().isEmpty()) {
            standardSolution.put("python", excel.getPythonSolution());
        }
        
        if (excel.getCppSolution() != null && !excel.getCppSolution().isEmpty()) {
            standardSolution.put("cpp", excel.getCppSolution());
        }
        
        if (excel.getCSolution() != null && !excel.getCSolution().isEmpty()) {
            standardSolution.put("c", excel.getCSolution());
        }
        
        if (!standardSolution.isEmpty()) {
            request.setStandardSolution(standardSolution);
        }
        
        // 处理其他字段
        request.setTimeLimit(excel.getTimeLimit());
        request.setMemoryLimit(excel.getMemoryLimit());
        request.setDifficulty(excel.getDifficulty());
        request.setType("PROGRAM"); // 固定为编程题
        request.setJobType(excel.getJobType());
        
        // 处理标签
        if (excel.getTags() != null && !excel.getTags().isEmpty()) {
            List<String> tags = new ArrayList<>();
            for (String tag : excel.getTags().split(",")) {
                tags.add(tag.trim());
            }
            request.setTags(tags);
        }
        
        return request;
    }
    
    /**
     * 根据文件名确定Excel类型
     */
    private ExcelTypeEnum determineExcelType(String fileName) {
        if (fileName.toLowerCase().endsWith(".csv")) {
            return ExcelTypeEnum.CSV;
        } else if (fileName.toLowerCase().endsWith(".xls")) {
            return ExcelTypeEnum.XLS;
        } else {
            return ExcelTypeEnum.XLSX; // 默认使用.xlsx格式
        }
    }
    
    /**
     * 构建导入结果
     */
    private Map<String, Object> buildImportResult(int totalCount, int successCount, Iterable<String> errorMessages) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failCount", totalCount - successCount);
        result.put("errorMessages", errorMessages);
        return result;
    }
} 