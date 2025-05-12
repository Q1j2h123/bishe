package com.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.model.entity.UserProblemStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

// 用户题目状态Mapper
@Mapper
public interface UserProblemStatusMapper extends BaseMapper<UserProblemStatus> {
    // 查询用户的题目状态
    @Select("SELECT * FROM user_problem_status WHERE userId = #{userId} AND problemId = #{problemId}")
    UserProblemStatus selectByUserAndProblem(@Param("userId") Long userId, @Param("problemId") Long problemId);
    
    // 插入或更新用户题目状态
    @Insert("INSERT INTO user_problem_status(userId, problemId, status, lastSubmitTime) " +
            "VALUES(#{userId}, #{problemId}, #{status}, NOW()) " +
            "ON DUPLICATE KEY UPDATE status = #{status}, lastSubmitTime = NOW()")
    int upsertStatus(@Param("userId") Long userId, @Param("problemId") Long problemId, @Param("status") String status);
}