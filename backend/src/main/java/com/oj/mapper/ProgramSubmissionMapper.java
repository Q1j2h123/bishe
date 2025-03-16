package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.ProgramSubmission;
import org.apache.ibatis.annotations.Mapper;

// 编程题提交Mapper
@Mapper
public interface ProgramSubmissionMapper extends BaseMapper<ProgramSubmission> {
}
