package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {
    
    /**
     * 增加题库题目数量
     */
    @Update("UPDATE question_bank SET problem_count = problem_count + 1 WHERE id = #{bankId}")
    int incrProblemCount(@Param("bankId") Long bankId);
    
    /**
     * 减少题库题目数量
     */
    @Update("UPDATE question_bank SET problem_count = problem_count - 1 WHERE id = #{bankId} AND problem_count > 0")
    int decrProblemCount(@Param("bankId") Long bankId);

    /**
     * 获取用户的题库列表
     */
    @Select("SELECT * FROM question_bank WHERE user_id = #{userId} AND is_delete = 0")
    List<QuestionBank> getByUserId(@Param("userId") Long userId);
} 