package com.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.entity.Testcase;
import com.oj.model.dto.TestCaseDTO;

import java.util.List;

/**
 * 测试用例服务接口
 */
public interface TestcaseService extends IService<Testcase> {
    
    /**
     * 添加测试用例
     * @param problemId 题目ID
     * @param input 输入数据
     * @param expectedOutput 期望输出
     * @param isExample 是否为示例
     * @return 测试用例ID
     */
    Long addTestcase(Long problemId, String input, String expectedOutput, Boolean isExample);
    
    /**
     * 更新测试用例
     * @param id 测试用例ID
     * @param input 输入数据
     * @param expectedOutput 期望输出
     * @param isExample 是否为示例
     * @return 是否成功
     */
    boolean updateTestcase(Long id, String input, String expectedOutput, Boolean isExample);
    
    /**
     * 删除测试用例
     * @param id 测试用例ID
     * @return 是否成功
     */
    boolean deleteTestcase(Long id);
    
    /**
     * 获取题目的所有测试用例
     * @param problemId 题目ID
     * @return 测试用例列表
     */
    List<TestCaseDTO> getTestcasesByProblemId(Long problemId);
    
    /**
     * 获取题目的示例测试用例
     * @param problemId 题目ID
     * @return 示例测试用例列表
     */
    List<TestCaseDTO> getExampleTestcases(Long problemId);
    
    /**
     * 分页获取题目的测试用例
     * @param problemId 题目ID
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    Page<TestCaseDTO> getTestcasesByPage(Long problemId, long current, long size);
    
    /**
     * 导入测试用例
     * @param problemId 题目ID
     * @param testCases 测试用例列表
     * @return 导入的数量
     */
    int importTestcases(Long problemId, List<TestCaseDTO> testCases);
} 