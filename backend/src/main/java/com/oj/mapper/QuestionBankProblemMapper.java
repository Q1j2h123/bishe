package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.QuestionBankProblem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库题目关联 Mapper
 */
@Mapper
public interface QuestionBankProblemMapper extends BaseMapper<QuestionBankProblem> {
} 