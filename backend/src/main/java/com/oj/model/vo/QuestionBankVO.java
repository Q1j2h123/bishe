package com.oj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.oj.model.entity.QuestionBank;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@ApiModel(description = "题库视图对象")
public class QuestionBankVO implements Serializable {
    
    @ApiModelProperty(value = "题库ID")
    private Long id;

    @ApiModelProperty(value = "题库名称")
    private String name;

    @ApiModelProperty(value = "题库描述")
    private String description;

    @ApiModelProperty(value = "难度（EASY-简单, MEDIUM-中等, HARD-困难）")
    private String difficulty;

    @ApiModelProperty(value = "标签列表")
    private List<String> tags;

    @ApiModelProperty(value = "权限（PUBLIC-公开, PRIVATE-私有, SHARED-共享）")
    private String permission;

    @ApiModelProperty(value = "是否热门（0-否, 1-是）")
    private Integer isHot;

    @ApiModelProperty(value = "是否推荐（0-否, 1-是）")
    private Integer isRecommended;

    @ApiModelProperty(value = "创建用户ID")
    private Long userId;

    @ApiModelProperty(value = "题目数量")
    private Integer problemCount;

    @ApiModelProperty(value = "题目ID列表")
    private List<Long> problemIds;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


    private static final long serialVersionUID = 1L;

    /**
     * 包装类转对象
     *
     * @param questionBankVO
     * @return
     */
    public static QuestionBank voToObj(QuestionBankVO questionBankVO) {
        if (questionBankVO == null) {
            return null;
        }
        QuestionBank questionBank = new QuestionBank();
        questionBank.setId(questionBankVO.getId());
        questionBank.setName(questionBankVO.getName());
        questionBank.setDescription(questionBankVO.getDescription());
        questionBank.setDifficulty(questionBankVO.getDifficulty());
        questionBank.setPermission(questionBankVO.getPermission());
        questionBank.setIsHot(questionBankVO.getIsHot());
        questionBank.setIsRecommended(questionBankVO.getIsRecommended());
        questionBank.setUserId(questionBankVO.getUserId());
        questionBank.setCreateTime(questionBankVO.getCreateTime() != null ? 
            LocalDateTime.ofInstant(questionBankVO.getCreateTime().toInstant(), ZoneId.systemDefault()) : null);
        questionBank.setUpdateTime(questionBankVO.getUpdateTime() != null ? 
            LocalDateTime.ofInstant(questionBankVO.getUpdateTime().toInstant(), ZoneId.systemDefault()) : null);
        return questionBank;
    }

    /**
     * 对象转包装类
     *
     * @param questionBank
     * @return
     */
    public static QuestionBankVO objToVo(QuestionBank questionBank) {
        if (questionBank == null) {
            return null;
        }
        QuestionBankVO questionBankVO = new QuestionBankVO();
        questionBankVO.setId(questionBank.getId());
        questionBankVO.setName(questionBank.getName());
        questionBankVO.setDescription(questionBank.getDescription());
        questionBankVO.setDifficulty(questionBank.getDifficulty());
        questionBankVO.setPermission(questionBank.getPermission());
        questionBankVO.setIsHot(questionBank.getIsHot());
        questionBankVO.setIsRecommended(questionBank.getIsRecommended());
        questionBankVO.setUserId(questionBank.getUserId());
        questionBankVO.setCreateTime(questionBank.getCreateTime() != null ? 
            Date.from(questionBank.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()) : null);
        questionBankVO.setUpdateTime(questionBank.getUpdateTime() != null ? 
            Date.from(questionBank.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant()) : null);
        return questionBankVO;
    }
} 