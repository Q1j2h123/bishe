package com.oj.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "用户管理详情视图对象")
public class UserManageVO {
    
    @ApiModelProperty(value = "用户ID")
    private Long id;

    @ApiModelProperty(value = "用户账号")
    private String userAccount;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    @ApiModelProperty(value = "用户简介")
    private String userProfile;

    @ApiModelProperty(value = "用户角色")
    private String userRole;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "提交次数")
    private Integer submissionCount;

    @ApiModelProperty(value = "通过题目数")
    private Integer acceptedCount;

    @ApiModelProperty(value = "总做题数")
    private Integer totalSolvedCount;

    @ApiModelProperty(value = "通过率")
    private Double acceptanceRate;

    @ApiModelProperty(value = "最近通过的题目")
    private String lastAcceptedProblem;
} 