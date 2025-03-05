package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.ProgrammingProblem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 编程题 Mapper
 */
@Mapper
public interface ProgrammingProblemMapper extends BaseMapper<ProgrammingProblem> {
} 