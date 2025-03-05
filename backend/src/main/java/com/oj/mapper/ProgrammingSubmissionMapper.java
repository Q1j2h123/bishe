package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.ProgrammingSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 编程题提交记录 Mapper
 */
@Mapper
public interface ProgrammingSubmissionMapper extends BaseMapper<ProgrammingSubmission> {
    
    /**
     * 获取用户某题目的所有提交记录
     */
    @Select("SELECT * FROM programming_submission WHERE userId = #{userId} AND problemId = #{problemId} ORDER BY submitTime DESC")
    List<ProgrammingSubmission> getUserProblemSubmissions(Long userId, Long problemId);

    /**
     * 获取用户的所有提交记录
     */
    @Select("SELECT * FROM programming_submission WHERE userId = #{userId} ORDER BY submitTime DESC")
    List<ProgrammingSubmission> getUserSubmissions(Long userId);

    /**
     * 获取题目的通过率
     */
    @Select("SELECT ROUND(COUNT(CASE WHEN status = 'ACCEPTED' THEN 1 END) * 100.0 / COUNT(*), 2) " +
            "FROM programming_submission WHERE problemId = #{problemId}")
    String getProblemAcceptRate(Long problemId);
} 