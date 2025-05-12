package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.Testcase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试用例Mapper
 */
@Mapper
public interface TestcaseMapper extends BaseMapper<Testcase> {
} 