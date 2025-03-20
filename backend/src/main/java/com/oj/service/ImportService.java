package com.oj.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 导入服务接口
 */
public interface ImportService {
    /**
     * 批量导入选择题
     *
     * @param file Excel文件
     * @param userId 用户ID
     * @return 导入结果 key:总数,成功数,失败数,错误信息
     * @throws IOException 如果文件读取失败
     */
    Map<String, Object> importChoiceProblems(MultipartFile file, Long userId) throws IOException;

    /**
     * 批量导入判断题
     *
     * @param file Excel文件
     * @param userId 用户ID
     * @return 导入结果 key:总数,成功数,失败数,错误信息
     * @throws IOException 如果文件读取失败
     */
    Map<String, Object> importJudgeProblems(MultipartFile file, Long userId) throws IOException;

    /**
     * 批量导入编程题
     *
     * @param file Excel文件
     * @param userId 用户ID
     * @return 导入结果 key:总数,成功数,失败数,错误信息
     * @throws IOException 如果文件读取失败
     */
    Map<String, Object> importProgramProblems(MultipartFile file, Long userId) throws IOException;
} 