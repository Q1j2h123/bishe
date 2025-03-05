package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库 Mapper
 */
@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {
} 