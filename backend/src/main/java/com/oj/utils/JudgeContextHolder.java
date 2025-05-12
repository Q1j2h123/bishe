package com.oj.utils;

/**
 * 判题上下文持有者
 * 用于在判题过程中传递提交ID等信息
 */
public class JudgeContextHolder {
    
    private static final ThreadLocal<Long> SUBMISSION_ID_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置当前线程的提交ID
     * 
     * @param submissionId 提交ID
     */
    public static void setSubmissionId(Long submissionId) {
        SUBMISSION_ID_HOLDER.set(submissionId);
    }
    
    /**
     * 获取当前线程的提交ID
     * 
     * @return 提交ID
     */
    public static Long getSubmissionId() {
        return SUBMISSION_ID_HOLDER.get();
    }
    
    /**
     * 清除当前线程的提交ID
     */
    public static void clear() {
        SUBMISSION_ID_HOLDER.remove();
    }
} 