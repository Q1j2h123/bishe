package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.ChoiceJudgeSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 选择判断题提交记录 Mapper
 */
@Mapper
public interface ChoiceJudgeSubmissionMapper extends BaseMapper<ChoiceJudgeSubmission> {
    
    /**
     * 获取用户某题目的所有提交记录
     */
    @Select("SELECT * FROM choice_judge_submission WHERE userId = #{userId} AND problemId = #{problemId} ORDER BY submitTime DESC")
    List<ChoiceJudgeSubmission> getUserProblemSubmissions(Long userId, Long problemId);

    /**
     * 获取用户的所有提交记录
     */
    @Select("SELECT * FROM choice_judge_submission WHERE userId = #{userId} ORDER BY submitTime DESC")
    List<ChoiceJudgeSubmission> getUserSubmissions(Long userId);

    /**
     * 获取题目的通过率
     */
    @Select("SELECT ROUND(COUNT(CASE WHEN isCorrect = 1 THEN 1 END) * 100.0 / COUNT(*), 2) " +
            "FROM choice_judge_submission WHERE problemId = #{problemId}")
    String getProblemAcceptRate(Long problemId);
} 