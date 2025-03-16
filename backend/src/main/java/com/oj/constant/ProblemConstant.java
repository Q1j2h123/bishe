package com.oj.constant;

public interface ProblemConstant {
    /**
     * 默认分页大小
     */
    int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    int MAX_PAGE_SIZE = 100;

    /**
     * 题目标题最大长度
     */
    int MAX_TITLE_LENGTH = 100;

    /**
     * 题目标签最大数量
     */
    int MAX_TAGS_COUNT = 5;

    /**
     * 编程题时间限制（毫秒）
     */
    int DEFAULT_TIME_LIMIT = 1000;

    /**
     * 编程题内存限制（MB）
     */
    int DEFAULT_MEMORY_LIMIT = 256;

    /**
     * 选择题选项最大数量
     */
    int MAX_CHOICE_OPTIONS = 26;

    
    /**
     * 岗位类型
     */
    String JOB_TYPE_FRONTEND = "前端开发";
    String JOB_TYPE_BACKEND = "后端开发";
    String JOB_TYPE_FULLSTACK = "全栈开发";
    String JOB_TYPE_ANDROID = "安卓开发";
    String JOB_TYPE_IOS = "iOS开发";
    String JOB_TYPE_DEVOPS = "运维开发";
    String JOB_TYPE_TESTING = "测试开发";
    String JOB_TYPE_ALGORITHM = "算法工程师";
    String JOB_TYPE_DATA = "数据工程师";
    String JOB_TYPE_AI = "人工智能";
    String JOB_TYPE_OTHER = "其他";
    
    /**
     * 获取所有岗位类型
     */
    String[] ALL_JOB_TYPES = {
        JOB_TYPE_FRONTEND,
        JOB_TYPE_BACKEND,
        JOB_TYPE_FULLSTACK,
        JOB_TYPE_ANDROID,
        JOB_TYPE_IOS,
        JOB_TYPE_DEVOPS,
        JOB_TYPE_TESTING,
        JOB_TYPE_ALGORITHM,
        JOB_TYPE_DATA,
        JOB_TYPE_AI,
        JOB_TYPE_OTHER
    };
} 