package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.Problem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {
    /**
     * 更新题目的提交次数和通过率
     */
    @Update("UPDATE problem SET submission_count = submission_count + 1, " +
            "accept_rate = CONCAT(ROUND((" +
            "SELECT COUNT(*) FROM program_submission ps " +
            "JOIN submission s ON ps.submission_id = s.id " +
            "WHERE s.problem_id = #{problemId} AND ps.status = 'ACCEPTED'" +
            ") * 100.0 / (submission_count + 1), 1), '%') " +
            "WHERE id = #{problemId}")
    int updateSubmissionCount(@Param("problemId") Long problemId);
} 