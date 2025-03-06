package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.QuestionBankProblem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionBankProblemMapper extends BaseMapper<QuestionBankProblem> {
    /**
     * 获取题库中的所有题目ID
     */
    @Select("SELECT problem_id FROM question_bank_problem " +
            "WHERE question_bank_id = #{bankId} AND is_delete = 0")
    List<Long> getProblemIdsByBankId(@Param("bankId") Long bankId);

    /**
     * 批量添加题目到题库
     */
    @Insert("<script>" +
            "INSERT INTO question_bank_problem (question_bank_id, problem_id) VALUES " +
            "<foreach collection='problemIds' item='problemId' separator=','>" +
            "(#{bankId}, #{problemId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("bankId") Long bankId, @Param("problemIds") List<Long> problemIds);

    /**
     * 批量移除题目
     */
    @Update("<script>" +
            "UPDATE question_bank_problem SET is_delete = 1 " +
            "WHERE question_bank_id = #{bankId} AND problem_id IN " +
            "<foreach collection='problemIds' item='problemId' open='(' separator=',' close=')'>" +
            "#{problemId}" +
            "</foreach>" +
            "</script>")
    int batchRemove(@Param("bankId") Long bankId, @Param("problemIds") List<Long> problemIds);
} 