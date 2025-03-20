package com.oj.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.constant.JudgeConstant;
import com.oj.model.dto.ExecutionResult;
import com.oj.service.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenAI服务实现类
 */
@Service
@Slf4j
public class OpenAIServiceImpl implements OpenAIService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 使用相同的配置前缀，但可以通过ai.openai前缀单独配置OpenAI服务
    @Value("${ai.openai.api-key:${ai.api-key:}}")
    private String apiKey;
    
    @Value("${ai.openai.model:${ai.model:gpt-3.5-turbo}}")
    private String model;
    
    @Value("${ai.openai.api-url:${ai.api-url:https://api.openai.com/v1}}")
    private String apiUrl;
    
    @Value("${ai.openai.endpoint:${ai.endpoint:/chat/completions}}")
    private String endpoint;
    
    @Value("${ai.openai.temperature:${ai.temperature:0.3}}")
    private double temperature;

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
            // 在OpenAI实现中，我们假设compiledCode就是实际的代码，不需要额外提取
            String realCode = compiledCode;
            if (compiledCode.startsWith("ai://")) {
                // 如果是通过AI执行器传入的路径，从DeepseekServiceImpl借用extractRealCode方法
                DeepseekServiceImpl deepseekService = new DeepseekServiceImpl();
                try {
                    java.lang.reflect.Method method = DeepseekServiceImpl.class.getDeclaredMethod("extractRealCode", String.class);
                    method.setAccessible(true);
                    realCode = (String) method.invoke(deepseekService, compiledCode);
                } catch (Exception e) {
                    log.error("尝试提取代码失败", e);
                    realCode = "// 无法获取原始代码";
                }
            }
            
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
        
        log.info("调用OpenAI API: {}", completeApiUrl);
        
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
        } catch (Exception e) {
            log.error("调用OpenAI API失败", e);
            throw new Exception("调用OpenAI API失败: " + e.getMessage(), e);
        }
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
    
    /**
     * 从文本中提取JSON对象
     */
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
} 