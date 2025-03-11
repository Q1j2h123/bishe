package com.oj.model.request;

import com.oj.model.dto.TestCaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProgramProblemAddRequest extends ProblemAddRequest {
    @NotBlank(message = "函数名称不能为空")
    private String functionName;

    @NotNull(message = "参数类型不能为空")
    private List<String> paramTypes;

    @NotBlank(message = "返回值类型不能为空")
    private String returnType;

    @NotNull(message = "测试用例不能为空")
    private List<TestCaseDTO> testCases;

    @NotNull(message = "代码模板不能为空")
    private Map<String, String> templates;

    @NotNull(message = "标准答案不能为空")
    private Map<String, String> standardSolution;

    private Integer timeLimit = 1000;  // 默认1000ms
    private Integer memoryLimit = 256;  // 默认256MB
} 