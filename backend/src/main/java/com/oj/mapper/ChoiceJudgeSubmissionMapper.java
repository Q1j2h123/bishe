package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.ChoiceJudgeSubmission;
import org.apache.ibatis.annotations.Mapper;

// 选择判断题提交Mapper
@Mapper
public interface ChoiceJudgeSubmissionMapper extends BaseMapper<ChoiceJudgeSubmission> {
}
