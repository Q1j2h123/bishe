package com.oj.model.request;

import com.oj.model.dto.TestCaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProgramProblemUpdateRequest extends ProblemUpdateRequest {
    /**
     * 函数名称
     */
    @NotBlank(message = "函数名称不能为空")
    private String functionName;

    /**
     * 参数类型列表
     */
    @NotEmpty(message = "参数类型不能为空")
    private List<String> paramTypes;

    /**
     * 返回值类型
     */
    @NotBlank(message = "返回值类型不能为空")
    private String returnType;

    /**
     * 测试用例列表
     */
    @NotEmpty(message = "测试用例不能为空")
    private List<TestCaseDTO> testCases;

    /**
     * 代码模板
     */
    @NotEmpty(message = "代码模板不能为空")
    private Map<String, String> templates;

    /**
     * 标准答案
     */
    private Map<String, String> standardSolution;

    /**
     * 时间限制(ms)
     */
    @NotNull(message = "时间限制不能为空")
    private Integer timeLimit;

    /**
     * 内存限制(MB)
     */
    @NotNull(message = "内存限制不能为空")
    private Integer memoryLimit;
} 