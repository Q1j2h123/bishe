package com.oj.model.vo;

import com.oj.model.dto.ChoiceProblemDTO;
import com.oj.model.dto.ProblemOptionDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "选择题视图对象")
public class ChoiceProblemVO extends ProblemVO {
    
    @ApiModelProperty(value = "选项列表")
    private List<ProblemOptionDTO> options;
    
    @ApiModelProperty(value = "正确答案")
    private String answer;
    
    @ApiModelProperty(value = "题目解析")
    private String analysis;

    /**
     * DTO转VO
     */
    public static ChoiceProblemVO dtoToVO(ChoiceProblemDTO dto) {
        if (dto == null) {
            return null;
        }
        ChoiceProblemVO vo = new ChoiceProblemVO();
        // 复制ProblemVO的字段
        vo.setId(dto.getId());
        vo.setTitle(dto.getTitle());
        vo.setContent(dto.getContent());
        vo.setType(dto.getType());
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
        
        // 复制选择题特有字段
        vo.setOptions(dto.getOptions());
        vo.setAnswer(dto.getAnswer());
        vo.setAnalysis(dto.getAnalysis());
        
        return vo;
    }
} 