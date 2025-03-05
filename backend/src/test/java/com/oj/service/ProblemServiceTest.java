package com.oj.service;

import com.oj.model.dto.ProblemExcelDTO;
import com.oj.model.entity.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProblemServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(ProblemServiceTest.class);

    @Resource
    private ProblemService problemService;

    @BeforeEach
    public void setUp() {
        // 清理数据库中的测试数据
        List<Problem> problems = problemService.list();
        for (Problem problem : problems) {
            problemService.removeById(problem.getId());
        }
    }

    @Test
    public void testImportProblems() throws IOException {
        // 读取测试文件
        File file = new File("src/test/resources/test_problems.xlsx");
        assertTrue(file.exists(), "测试文件不存在");
        
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel", input);

        // 测试导入
        boolean result = problemService.importProblems(multipartFile);
        assertTrue(result, "导入失败");

        // 验证导入的数据
        List<Problem> problems = problemService.list();
        assertFalse(problems.isEmpty(), "导入后没有数据");
        assertEquals(3, problems.size(), "导入的数据数量不正确");

        // 验证数据内容
        Problem problem = problems.get(0);
        assertEquals("Java基础题1", problem.getTitle(), "题目标题不正确");
        assertEquals("单选题", problem.getType(), "题目类型不正确");
        assertEquals("EASY", problem.getDifficulty(), "题目难度不正确");
        
        // 验证选项内容（单选题）
        assertNotNull(problem.getOptions(), "单选题选项不能为空");
        assertTrue(problem.getOptions().contains("A."), "单选题必须包含选项A");
        
        // 验证编程题内容
        problem = problems.get(1);
        assertEquals("算法题1", problem.getTitle(), "题目标题不正确");
        assertEquals("编程题", problem.getType(), "题目类型不正确");
        assertNotNull(problem.getAnswer(), "编程题答案不能为空");
        assertTrue(problem.getAnswer().contains("class Solution"), "编程题答案必须包含代码实现");
        
        // 验证SQL题内容
        problem = problems.get(2);
        assertEquals("SQL题1", problem.getTitle(), "题目标题不正确");
        assertEquals("SQL题", problem.getType(), "题目类型不正确");
        assertNotNull(problem.getAnswer(), "SQL题答案不能为空");
        assertTrue(problem.getAnswer().toLowerCase().contains("select"), "SQL题答案必须包含SQL语句");
        
        logger.info("成功导入{}条数据", problems.size());
    }

    @Test
    public void testExportProblems() throws IOException {
        // 先导入测试数据
        File file = new File("src/test/resources/test_problems.xlsx");
        assertTrue(file.exists(), "测试文件不存在");
        
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel", input);
        boolean importResult = problemService.importProblems(multipartFile);
        assertTrue(importResult, "导入测试数据失败");

        // 获取所有题目ID
        List<Problem> problems = problemService.list();
        assertFalse(problems.isEmpty(), "没有可导出的数据");
        
        List<Long> problemIds = problems.stream().map(Problem::getId).collect(Collectors.toList());
        assertFalse(problemIds.isEmpty(), "没有可导出的题目ID");

        // 创建模拟的HttpServletResponse
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=题目列表.xlsx");

        // 测试导出
        problemService.exportProblems(response, problemIds);

        // 验证响应头和数据
        assertTrue(response.getContentType().startsWith("application/vnd.ms-excel"), "响应类型不正确");
        byte[] content = response.getContentAsByteArray();
        assertTrue(content.length > 0, "导出数据为空");
        
        // TODO: 解析导出的Excel文件，验证数据完整性
        // ByteArrayInputStream bis = new ByteArrayInputStream(content);
        // EasyExcel.read(bis, ProblemExcelDTO.class, new ProblemDataListener()).sheet().doRead();
        
        logger.info("成功导出{}条数据", problemIds.size());
    }
} 