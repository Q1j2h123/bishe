package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.Problem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 题目 Mapper
 */
@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {
    
    /**
     * 复杂条件查询
     */
    List<Problem> searchProblems(@Param("title") String title,
                                @Param("content") String content,
                                @Param("type") String type,
                                @Param("jobType") String jobType,
                                @Param("difficulty") String difficulty,
                                @Param("tags") String tags,
                                @Param("sortField") String sortField,
                                @Param("sortOrder") String sortOrder);

    /**
     * 获取题目详情（包含具体类型的信息）
     */
    Problem getProblemDetail(@Param("id") Long id);
} 