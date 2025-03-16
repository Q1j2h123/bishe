package com.oj.constant;

/**
 * 评测相关常量
 */
public interface JudgeConstant {
    
    /**
     * 支持的编程语言
     */
    String LANGUAGE_JAVA = "java";
    String LANGUAGE_PYTHON = "python";
    String LANGUAGE_CPP = "cpp";
    String LANGUAGE_C = "c";
    
    /**
     * 评测状态
     */
    String STATUS_PENDING = "PENDING";       // 等待评测
    String STATUS_JUDGING = "JUDGING";       // 评测中
    String STATUS_ACCEPTED = "ACCEPTED";     // 通过
    String STATUS_WRONG_ANSWER = "WRONG_ANSWER";         // 答案错误
    String STATUS_TIME_LIMIT_EXCEEDED = "TIME_LIMIT_EXCEEDED";   // 超时
    String STATUS_MEMORY_LIMIT_EXCEEDED = "MEMORY_LIMIT_EXCEEDED"; // 内存超限
    String STATUS_RUNTIME_ERROR = "RUNTIME_ERROR";  // 运行时错误
    String STATUS_COMPILE_ERROR = "COMPILE_ERROR";  // 编译错误
    String STATUS_SYSTEM_ERROR = "SYSTEM_ERROR";    // 系统错误
    
    /**
     * Docker相关
     */
    String DOCKER_IMAGE_JAVA = "openjdk:11-jdk-slim";
    String DOCKER_IMAGE_PYTHON = "python:3.9-slim";
    String DOCKER_IMAGE_CPP = "gcc:latest";
    
    /**
     * 执行目录
     */
    String JUDGE_WORKSPACE = "/tmp/judge/";
    
    /**
     * 超时限制（默认）单位：毫秒
     */
    Integer DEFAULT_TIME_LIMIT = 1000;  // 1秒
    
    /**
     * 内存限制（默认）单位：MB
     */
    Integer DEFAULT_MEMORY_LIMIT = 256;  // 256MB
    
    /**
     * 内存限制转换（KB）单位：KB
     */
    Long DEFAULT_MEMORY_LIMIT_KB = 256L * 1024;  // 256MB转KB
} 