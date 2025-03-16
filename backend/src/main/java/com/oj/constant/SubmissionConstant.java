package com.oj.constant;

/**
 * 提交相关常量
 */
public interface SubmissionConstant {
    
    /**
     * 提交类型
     */
    String TYPE_CHOICE = "CHOICE";    // 选择题
    String TYPE_JUDGE = "JUDGE";      // 判断题
    String TYPE_PROGRAM = "PROGRAM";  // 编程题
    
    /**
     * 提交状态
     */
    // 选择/判断题状态
    String STATUS_CORRECT = "CORRECT";  // 正确
    String STATUS_WRONG = "WRONG";      // 错误
    
    // 编程题状态
    String STATUS_PENDING = "PENDING";       // 待评测
    String STATUS_JUDGING = "JUDGING";       // 评测中
    String STATUS_ACCEPTED = "ACCEPTED";     // 通过
    String STATUS_WRONG_ANSWER = "WRONG_ANSWER";         // 答案错误
    String STATUS_TIME_LIMIT_EXCEEDED = "TIME_LIMIT_EXCEEDED";   // 超时
    String STATUS_MEMORY_LIMIT_EXCEEDED = "MEMORY_LIMIT_EXCEEDED"; // 内存超限
    String STATUS_RUNTIME_ERROR = "RUNTIME_ERROR";  // 运行时错误
    String STATUS_COMPILE_ERROR = "COMPILE_ERROR";  // 编译错误
    
    /**
     * 用户题目状态
     */
    String USER_STATUS_UNSOLVED = "UNSOLVED";   // 未解决
    String USER_STATUS_ATTEMPTED = "ATTEMPTED"; // 已尝试
    String USER_STATUS_SOLVED = "SOLVED";       // 已解决
}