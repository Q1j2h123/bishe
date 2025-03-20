package com.oj.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.constant.JudgeConstant;
import com.oj.model.dto.ExecutionResult;
import com.oj.service.DeepseekService;
import com.oj.util.JudgeContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deepseek服务实现类
 */
@Service
@Slf4j
public class DeepseekServiceImpl implements DeepseekService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ai.api-key}")
    private String apiKey;
    
    @Value("${ai.model:deepseek-chat}")
    private String model;
    
    @Value("${ai.api-url:https://api.deepseek.com/v1}")
    private String apiUrl;
    
    @Value("${ai.endpoint:/chat/completions}")
    private String endpoint;
    
    @Value("${ai.temperature:0.3}")
    private double temperature;

    @Value("${ai.provider:deepseek}")
    private String provider;
    
    @Resource
    private Environment environment;
    
    // 缓存提交的代码，避免文件系统读取
    private final Map<String, String> codeCache = new HashMap<>();
    
    @Override
    public boolean validateCodeSyntax(String code, String language) {
        log.info("验证代码语法，语言: {}", language);
        
        try {
            String prompt = String.format(
                    "请分析以下%s代码是否有语法错误，只需回答'是'或'否'。代码：\n\n%s",
                    language, code);
            
            String response = callAI(prompt);
            // 简单检查回复中是否包含"否"或"没有语法错误"等表示代码正确的关键词
            return !response.contains("是") && 
                  (response.contains("否") || 
                   response.contains("没有语法错误") || 
                   response.contains("语法正确") ||
                   response.contains("代码语法正确"));
        } catch (Exception e) {
            log.error("验证代码语法时出错", e);
            // 如果API调用失败，假设代码正确
            return true;
        }
    }
    
    @Override
    public String getCompileErrorMessage(String code, String language) {
        log.info("获取编译错误信息，语言: {}", language);
        
        try {
            String prompt = String.format(
                    "请分析以下%s代码的语法错误，并提供详细的错误信息和修复建议。代码：\n\n%s",
                    language, code);
            
            return callAI(prompt);
        } catch (Exception e) {
            log.error("获取编译错误信息时出错", e);
            return "无法获取具体错误信息：" + e.getMessage();
        }
    }
    
    @Override
    public ExecutionResult evaluateCode(String compiledCode, String input, String language) {
        log.info("评估代码执行结果，语言: {}, 输入: {}", language, input);
        
        try {
            // 获取真实代码
            String realCode = extractRealCode(compiledCode);
            
            if (realCode.startsWith("//") && realCode.contains("无法获取原始代码")) {
                log.error("无法获取原始代码，评估失败");
                return ExecutionResult.builder()
                        .success(false)
                        .status(JudgeConstant.STATUS_RUNTIME_ERROR)
                        .errorMessage("无法获取原始代码，无法执行")
                        .build();
            }
            
            String prompt = String.format(
                    "请执行以下%s代码，并分析其性能。输入是：\n\"%s\"\n\n代码：\n%s\n\n" +
                    "必须返回以下JSON格式（不要返回其他格式或代码）：\n" +
                    "{\n" +
                    "  \"output\": \"输出结果\",\n" +
                    "  \"success\": true或false,\n" +
                    "  \"executionTime\": 数字,\n" +
                    "  \"memoryUsage\": 数字,\n" +
                    "  \"errorMessage\": \"错误信息（如有）\"\n" +
                    "}",
                    language, input, realCode);
            
            String response = callAI(prompt);
            
            // 从回复中提取JSON部分
            String jsonStr = extractJson(response);
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            
            boolean success = jsonNode.has("success") ? jsonNode.get("success").asBoolean() : false;
            String output = jsonNode.has("output") ? jsonNode.get("output").asText() : "";
            Long executionTime = jsonNode.has("executionTime") ? jsonNode.get("executionTime").asLong() : 100L;
            Long memoryUsage = jsonNode.has("memoryUsage") ? jsonNode.get("memoryUsage").asLong() : 10240L;
            String errorMessage = jsonNode.has("errorMessage") ? jsonNode.get("errorMessage").asText() : "";
            
            String status = success ? JudgeConstant.STATUS_ACCEPTED : JudgeConstant.STATUS_RUNTIME_ERROR;
            
            return ExecutionResult.builder()
                    .success(success)
                    .status(status)
                    .output(output)
                    .executeTime(executionTime)
                    .memoryUsage(memoryUsage)
                    .errorMessage(errorMessage)
                    .build();
        } catch (Exception e) {
            log.error("评估代码执行结果时出错", e);
            return ExecutionResult.builder()
                    .success(false)
                    .status(JudgeConstant.STATUS_SYSTEM_ERROR)
                    .errorMessage("AI评估出错: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 通用AI调用方法，从配置中读取参数，构建请求
     */
    private String callAI(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("model", model);
        requestBody.put("messages", new Object[]{message});
        requestBody.put("temperature", temperature);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        // 构建完整API URL
        String completeApiUrl = buildApiUrl();
        
        log.info("调用AI API ({}): {}", provider, completeApiUrl);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(completeApiUrl, request, String.class);
            
            if (response == null || response.getBody() == null) {
                log.error("API响应为空");
                return "API调用失败: 服务器返回空响应";
            }
            
            String responseBody = response.getBody();
            log.debug("API原始响应: {}", responseBody);
            
            JsonNode responseJson = objectMapper.readTree(responseBody);
            
            if (responseJson == null) {
                log.error("无法解析API响应为JSON");
                return "解析响应失败";
            }
            
            return extractContentFromResponse(responseJson);
        } catch (Exception e) {
            log.error("调用AI API失败", e);
            throw new Exception("调用AI API失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据提供商不同，从响应中提取内容
     */
    private String extractContentFromResponse(JsonNode responseJson) {
        // 通用提取逻辑
        JsonNode choicesNode = responseJson.path("choices");
        if (choicesNode.isMissingNode() || choicesNode.isEmpty()) {
            log.error("API响应中缺少choices字段");
            return "API响应格式不正确: 缺少choices字段";
        }
        
        JsonNode firstChoice = choicesNode.get(0);
        if (firstChoice == null) {
            log.error("API响应中的choices数组为空");
            return "API响应格式不正确: choices数组为空";
        }
        
        // 不同提供商的响应结构可能不同
        if ("openai".equalsIgnoreCase(provider) || "qwen".equalsIgnoreCase(provider) || 
            "deepseek".equalsIgnoreCase(provider) || "dashscope".equalsIgnoreCase(provider)) {
            JsonNode messageNode = firstChoice.path("message");
            if (messageNode.isMissingNode()) {
                log.error("API响应中缺少message字段");
                return "API响应格式不正确: 缺少message字段";
            }
            
            JsonNode contentNode = messageNode.path("content");
            if (contentNode.isMissingNode()) {
                log.error("API响应中缺少content字段");
                return "API响应格式不正确: 缺少content字段";
            }
            
            return contentNode.asText();
        }
        
        // 其他提供商的格式处理可以在这里添加
        
        // 默认尝试直接获取text字段
        if (firstChoice.has("text")) {
            return firstChoice.get("text").asText();
        }
        
        log.error("无法从响应中提取内容");
        return "API响应格式不支持: 无法识别的格式";
    }
    
    /**
     * 根据配置构建API URL
     */
    private String buildApiUrl() {
        String url = apiUrl;
        
        // 确保URL不以/结尾，endpoint以/开头
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        
        String endpointPath = endpoint;
        if (!endpointPath.startsWith("/")) {
            endpointPath = "/" + endpointPath;
        }
        
        return url + endpointPath;
    }

    private String extractRealCode(String compiledCode) {
        // 实际的代码应该直接在program_submission表中，这里只是尝试从文件系统获取更多信息
        try {
            log.info("提取代码: {}", compiledCode);
            
            // 如果是真实代码直接返回（检查是否包含class、def、function等关键字）
            if (compiledCode != null && 
                (compiledCode.contains("class") || 
                 compiledCode.contains("def ") || 
                 compiledCode.contains("function ") || 
                 compiledCode.contains("int ") ||
                 compiledCode.contains("void "))) {
                log.info("识别为直接代码，无需额外处理");
                return compiledCode;
            }
            
            // 如果是从缓存中获取
            if (codeCache.containsKey(compiledCode)) {
                String cachedCode = codeCache.get(compiledCode);
                log.info("从缓存获取代码");
                return cachedCode;
            }
            
            // 从ai://language/solution格式的路径中提取代码
            if (compiledCode != null && compiledCode.startsWith("ai://")) {
                log.info("识别为AI路径格式: {}", compiledCode);
                
                // 首先尝试从compiledCode中解析提交ID
                String language = "java";
                String submissionId = null;
                
                // 尝试通过正则表达式从compiledCode中提取
                Pattern pattern = Pattern.compile("ai://([^/]+)/solution(?:_([0-9]+))?");
                Matcher matcher = pattern.matcher(compiledCode);
                
                if (matcher.find()) {
                    language = matcher.group(1);
                    if (matcher.groupCount() >= 2) {
                        submissionId = matcher.group(2);
                    }
                }
                
                // 如果无法从compiledCode中获取提交ID，则从线程上下文中获取
                if (submissionId == null || submissionId.equals("unknown")) {
                    Long currentSubmissionId = JudgeContextHolder.getSubmissionId();
                    if (currentSubmissionId != null) {
                        submissionId = currentSubmissionId.toString();
                        log.info("从上下文获取提交ID: {}", submissionId);
                    } else {
                        log.warn("无法从上下文获取提交ID");
                        submissionId = "unknown";
                    }
                }
                
                // 读取配置文件中的AI评测工作目录
                String aiWorkspace = environment.getProperty("judge.ai-workspace", "E:/temp/oj-project/ai-judge");
                log.info("使用AI工作目录: {}", aiWorkspace);
                
                String workspacePath = aiWorkspace + "/" + submissionId;
                String fileName = "Solution." + getFileExtension(language);
                File codeFile = new File(workspacePath, fileName);
                log.info("尝试读取文件: {}", codeFile.getAbsolutePath());
                
                if (codeFile.exists()) {
                    String codeContent = new String(java.nio.file.Files.readAllBytes(codeFile.toPath()));
                    codeCache.put(compiledCode, codeContent);
                    log.info("成功读取文件并缓存代码");
                    return codeContent;
                } else {
                    log.warn("代码文件不存在: {}", codeFile.getAbsolutePath());
                    
                    // 如果找不到指定提交ID的文件，则尝试搜索所有提交文件夹
                    File aiDir = new File(aiWorkspace);
                    if (aiDir.exists() && aiDir.isDirectory()) {
                        // 获取所有子目录
                        File[] subDirs = aiDir.listFiles(File::isDirectory);
                        if (subDirs != null) {
                            // 按照修改时间倒序排列，优先使用最新创建的文件
                            java.util.Arrays.sort(subDirs, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                            
                            for (File dir : subDirs) {
                                File potentialFile = new File(dir, fileName);
                                if (potentialFile.exists()) {
                                    log.info("找到最新的代码文件: {}，最后修改时间: {}", 
                                            potentialFile.getAbsolutePath(), 
                                            new java.util.Date(potentialFile.lastModified()));
                                    String codeContent = new String(java.nio.file.Files.readAllBytes(potentialFile.toPath()));
                                    codeCache.put(compiledCode, codeContent);
                                    return codeContent;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("读取代码文件失败", e);
        }
        
        // 如果无法读取文件，返回一个默认代码
        log.warn("无法获取原始代码");
        return "// 无法获取原始代码";
    }
    
    /**
     * 根据语言获取文件扩展名
     */
    private String getFileExtension(String language) {
        if (language == null) {
            return "txt";
        }
        
        switch (language.toLowerCase()) {
            case "java":
                return "java";
            case "python":
                return "py";
            case "cpp":
            case "c++":
                return "cpp";
            case "c":
                return "c";
            case "javascript":
            case "js":
                return "js";
            case "csharp":
            case "c#":
                return "cs";
            default:
                return "txt";
        }
    }
    
    private String extractJson(String text) {
        // 尝试找到完整的JSON对象
        Pattern pattern = Pattern.compile("\\{[^{}]*((\\{[^{}]*\\})[^{}]*)*\\}");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        // 如果找不到JSON，返回一个默认的
        return "{\"success\": false, \"errorMessage\": \"无法从AI响应中提取JSON\"}";
    }

    @Test

    public void testApiConnection() {
        log.info("=== AI API 测试 ===");
        
        // 手动设置属性值，避免依赖Spring上下文
        if (this.apiUrl == null) {
            this.apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
            log.info("测试设置API URL: {}", this.apiUrl);
        }
        
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            this.apiKey = "sk-90c41fa06627493a878b6afb10470472"; // 使用application.properties中的值
            log.info("测试设置API Key: {}", this.apiKey.substring(0, 4) + "..." + this.apiKey.substring(this.apiKey.length() - 4));
        }
        
        if (this.model == null) {
            this.model = "qwen-turbo";
            log.info("测试设置模型: {}", this.model);
        }
        
        if (this.provider == null) {
            this.provider = "qwen";
            log.info("测试设置提供商: {}", this.provider);
        }
        
        if (this.temperature == 0) {
            this.temperature = 0.3;
            log.info("测试设置温度: {}", this.temperature);
        }
        
        log.info("API URL: {}", apiUrl);
        log.info("API Key: {}", apiKey != null ? (apiKey.substring(0, 4) + "..." + (apiKey.length() > 8 ? apiKey.substring(apiKey.length() - 4) : "")) : "未设置");
        log.info("模型: {}", model);
        log.info("提供商: {}", provider);
        
        if (apiUrl == null || apiUrl.isEmpty()) {
            log.error("API URL未设置");
            Assert.fail("API URL未设置");
            return;
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("API Key未设置");
            Assert.fail("API Key未设置");
            return;
        }
        
        try {
            String simplePrompt = "Hello, this is a test message. Please respond with a short greeting.";
            log.info("测试提示: {}", simplePrompt);
            
            String response = callAI(simplePrompt);
            log.info("API测试响应: {}", response);
            
            // 检查响应是否有效
            Assert.assertNotNull("API响应不应为空", response);
            Assert.assertFalse("API响应不应包含错误信息", 
                response.contains("API调用失败") || 
                response.contains("解析响应失败") ||
                response.contains("API响应格式不正确"));
            
            log.info("AI API连接测试成功！");
        } catch (Exception e) {
            log.error("API测试失败", e);
            Assert.fail("API调用异常: " + e.getMessage());
        }
    }
} 