package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.exception.BusinessException;
import com.oj.mapper.ProblemMapper;
import com.oj.mapper.TestcaseMapper;
import com.oj.model.dto.TestCaseDTO;
import com.oj.model.entity.Problem;
import com.oj.model.entity.Testcase;
import com.oj.service.TestcaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试用例服务实现
 */
@Service
@Slf4j
public class TestcaseServiceImpl extends ServiceImpl<TestcaseMapper, Testcase> implements TestcaseService {
    
    @Resource
    private ProblemMapper problemMapper;
    
    @Override
    @Transactional
    public Long addTestcase(Long problemId, String input, String expectedOutput, Boolean isExample) {
        // 检查题目是否存在
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(40000, "题目不存在");
        }
        
        // 创建测试用例
        Testcase testcase = new Testcase();
        testcase.setProblemId(problemId);
        testcase.setInput(input);
        testcase.setExpectedOutput(expectedOutput);
        testcase.setIsExample(isExample);
        testcase.setCreateTime(LocalDateTime.now());
        testcase.setUpdateTime(LocalDateTime.now());
        testcase.setIsDelete(0);
        
        // 保存测试用例
        this.save(testcase);
        return testcase.getId();
    }
    
    @Override
    @Transactional
    public boolean updateTestcase(Long id, String input, String expectedOutput, Boolean isExample) {
        // 检查测试用例是否存在
        Testcase testcase = this.getById(id);
        if (testcase == null) {
            throw new BusinessException(40000, "测试用例不存在");
        }
        
        // 更新测试用例
        testcase.setInput(input);
        testcase.setExpectedOutput(expectedOutput);
        testcase.setIsExample(isExample);
        testcase.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(testcase);
    }
    
    @Override
    @Transactional
    public boolean deleteTestcase(Long id) {
        // 检查测试用例是否存在
        Testcase testcase = this.getById(id);
        if (testcase == null) {
            throw new BusinessException(40000, "测试用例不存在");
        }
        
        // 逻辑删除测试用例
        testcase.setIsDelete(1);
        testcase.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(testcase);
    }
    
    @Override
    public List<TestCaseDTO> getTestcasesByProblemId(Long problemId) {
        // 检查题目是否存在
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(40000, "题目不存在");
        }
        
        // 查询测试用例
        LambdaQueryWrapper<Testcase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Testcase::getProblemId, problemId)
                  .eq(Testcase::getIsDelete, 0);
        
        List<Testcase> testcases = this.list(queryWrapper);
        
        // 转换为DTO
        return testcases.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public List<TestCaseDTO> getExampleTestcases(Long problemId) {
        // 检查题目是否存在
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(40000, "题目不存在");
        }
        
        // 查询示例测试用例
        LambdaQueryWrapper<Testcase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Testcase::getProblemId, problemId)
                  .eq(Testcase::getIsExample, true)
                  .eq(Testcase::getIsDelete, 0);
        
        List<Testcase> testcases = this.list(queryWrapper);
        
        // 转换为DTO
        return testcases.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public Page<TestCaseDTO> getTestcasesByPage(Long problemId, long current, long size) {
        // 检查题目是否存在
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(40000, "题目不存在");
        }
        
        // 查询测试用例
        LambdaQueryWrapper<Testcase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Testcase::getProblemId, problemId)
                  .eq(Testcase::getIsDelete, 0)
                  .orderByDesc(Testcase::getCreateTime);
        
        Page<Testcase> page = this.page(new Page<>(current, size), queryWrapper);
        
        // 转换为DTO
        Page<TestCaseDTO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<TestCaseDTO> records = page.getRecords().stream()
                                       .map(this::convertToDTO)
                                       .collect(Collectors.toList());
        resultPage.setRecords(records);
        
        return resultPage;
    }
    
    @Override
    @Transactional
    public int importTestcases(Long problemId, List<TestCaseDTO> testCases) {
        // 检查题目是否存在
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(40000, "题目不存在");
        }
        
        if (testCases == null || testCases.isEmpty()) {
            return 0;
        }
        
        List<Testcase> entities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (TestCaseDTO dto : testCases) {
            Testcase testcase = new Testcase();
            testcase.setProblemId(problemId);
            testcase.setInput(dto.getInput());
            testcase.setExpectedOutput(dto.getOutput()); // 注意DTO中是output而实体是expectedOutput
            testcase.setIsExample(false); // 导入的测试用例默认不是示例
            testcase.setCreateTime(now);
            testcase.setUpdateTime(now);
            testcase.setIsDelete(0);
            entities.add(testcase);
        }
        
        // 批量保存
        this.saveBatch(entities);
        return entities.size();
    }
    
    /**
     * 将实体转换为DTO
     */
    private TestCaseDTO convertToDTO(Testcase testcase) {
        if (testcase == null) {
            return null;
        }
        
        TestCaseDTO dto = new TestCaseDTO();
        dto.setInput(testcase.getInput());
        dto.setOutput(testcase.getExpectedOutput()); // 注意映射关系
        
        return dto;
    }
} 