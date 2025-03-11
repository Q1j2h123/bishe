package com.oj.model.vo;

import com.oj.model.dto.JudgeProblemDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "判断题视图对象")
public class JudgeProblemVO extends ProblemVO {
    
    @ApiModelProperty(value = "正确答案")
    private Boolean answer;
    
    @ApiModelProperty(value = "题目解析")
    private String analysis;

    /**
     * DTO转VO
     */
    public static JudgeProblemVO dtoToVO(JudgeProblemDTO dto) {
        if (dto == null) {
            return null;
        }
        JudgeProblemVO vo = new JudgeProblemVO();
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
        
        // 复制判断题特有字段
        vo.setAnswer(dto.getAnswer());
        vo.setAnalysis(dto.getAnalysis());
        
        return vo;
    }
} 