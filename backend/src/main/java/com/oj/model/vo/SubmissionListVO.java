package com.oj.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// 基础提交VO
@Data
public class SubmissionListVO {
    private Long id;
    private Long problemId;
    private String problemTitle;
    private String type;
    private String status;
    private LocalDateTime submissionTime;
    private String difficulty;
    private String jobType;
    private List<String> tags;
}