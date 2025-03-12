package com.oj.model.vo;

import com.oj.model.dto.ProgramProblemDTO;
import com.oj.model.dto.TestCaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "编程题视图对象")
public class ProgramProblemVO extends ProblemVO {
    
    @ApiModelProperty(value = "函数名")
    private String functionName;
    
    @ApiModelProperty(value = "参数类型列表")
    private List<String> paramTypes;
    
    @ApiModelProperty(value = "返回值类型")
    private String returnType;
    
    @ApiModelProperty(value = "测试用例列表")
    private List<TestCaseDTO> testCases;
    
    @ApiModelProperty(value = "代码模板")
    private Map<String, String> templates;
    
    @ApiModelProperty(value = "标准答案")
    private Map<String, String> standardSolution;
    
    @ApiModelProperty(value = "时间限制(ms)")
    private Integer timeLimit;
    
    @ApiModelProperty(value = "内存限制(MB)")
    private Integer memoryLimit;

    /**
     * DTO转VO
     */
    public static ProgramProblemVO dtoToVO(ProgramProblemDTO dto) {
        if (dto == null) {
            return null;
        }
        ProgramProblemVO vo = new ProgramProblemVO();
        // 复制ProblemVO的字段
        vo.setId(dto.getId());
        vo.setTitle(dto.getTitle());
        vo.setContent(dto.getContent());
        vo.setType(dto.getType());
        vo.setStatus(dto.getStatus());
        vo.setJobType(dto.getJobType());
        vo.setDifficulty(dto.getDifficulty());
        vo.setTags(StringUtils.isNotBlank(dto.getTags()) ? Arrays.asList(dto.getTags().split(",")) : null);
        vo.setAcceptRate(dto.getAcceptRate());
        vo.setSubmissionCount(dto.getSubmissionCount());
        vo.setUserId(dto.getUserId());
        vo.setCreateTime(dto.getCreateTime() != null ? 
            Date.from(dto.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()) : null);
        vo.setUpdateTime(dto.getUpdateTime() != null ? 
            Date.from(dto.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant()) : null);
        
        // 复制编程题特有字段
        vo.setFunctionName(dto.getFunctionName());
        vo.setParamTypes(dto.getParamTypes());
        vo.setReturnType(dto.getReturnType());
        vo.setTestCases(dto.getTestCases());
        vo.setTemplates(dto.getTemplates());
        vo.setStandardSolution(dto.getStandardSolution());
        vo.setTimeLimit(dto.getTimeLimit());
        vo.setMemoryLimit(dto.getMemoryLimit());
        
        return vo;
    }
} 