package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.DraftSolution;
import org.apache.ibatis.annotations.Mapper;

/**
 * 草稿解答数据访问层
 */
@Mapper
public interface DraftSolutionMapper extends BaseMapper<DraftSolution> {
} 