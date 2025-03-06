package com.oj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.oj.model.entity.QuestionBank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Schema(description = "题库视图对象")
public class QuestionBankVO implements Serializable {
    
    @Schema(description = "题库id")
    private Long id;

    @Schema(description = "题库名称")
    private String name;

    @Schema(description = "题库描述")
    private String description;

    @Schema(description = "难度（EASY-简单, MEDIUM-中等, HARD-困难）")
    private String difficulty;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "权限（PUBLIC-公开, PRIVATE-私有, SHARED-共享）")
    private String permission;

    @Schema(description = "是否热门（0-否, 1-是）")
    private Integer isHot;

    @Schema(description = "是否推荐（0-否, 1-是）")
    private Integer isRecommended;

    @Schema(description = "创建用户id")
    private Long userId;

    @Schema(description = "题目数量")
    private Integer problemCount;

    @Schema(description = "题目id列表")
    private List<Long> problemIds;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "更新时间")
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