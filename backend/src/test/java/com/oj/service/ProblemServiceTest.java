//package com.oj.service;
//
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.oj.model.dto.ProblemDTO;
//import com.oj.model.entity.Problem;
//import com.oj.model.request.ProblemAddRequest;
//import com.oj.model.request.ProblemQueryRequest;
//import com.oj.model.request.ProblemUpdateRequest;
//import com.oj.model.vo.ProblemVO;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import javax.annotation.Resource;
//import java.util.Arrays;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//public class ProblemServiceTest {
//
//    @Resource
//    private ProblemService problemService;
//
//
//
//    @Test
//    public void testUpdateProblem() {
//        // 准备测试数据
//        ProblemUpdateRequest request = new ProblemUpdateRequest();
//        request.setId(1L);
//        request.setTitle("更新后的题目");
//        request.setContent("更新后的内容");
//        request.setType("program");
//        request.setDifficulty("MEDIUM");
//        request.setTags("算法,编程,数据结构");
//        request.setJobType("开发");
//        request.setProblemDetail("{\"description\":\"更新后的描述\",\"inputFormat\":\"更新后的输入格式\",\"outputFormat\":\"更新后的输出格式\",\"sampleInput\":\"更新后的样例输入\",\"sampleOutput\":\"更新后的样例输出\"}");
//
//        // 执行测试
//        boolean result = problemService.updateProblem(request, 1L);
////
//        // 验证结果
//        assertTrue(result);
//    }
//
//    @Test
//    public void testDeleteProblem() {
//        // 执行测试
//        boolean result = problemService.deleteProblem(1L, 1L);
//
//        // 验证结果
//        assertTrue(result);
//    }
//
//    @Test
//    public void testGetProblemById() {
//        // 执行测试
//        ProblemVO problemVO = problemService.getProblemById(1L, 1L);
//
//        // 验证结果
//        assertNotNull(problemVO);
//        assertEquals("更新后的题目", problemVO.getTitle());
//        assertEquals("更新后的内容", problemVO.getContent());
//        assertEquals("program", problemVO.getType());
//        assertEquals("MEDIUM", problemVO.getDifficulty());
//        assertTrue(problemVO.getTags().contains("算法"));
//        assertTrue(problemVO.getTags().contains("编程"));
//        assertTrue(problemVO.getTags().contains("数据结构"));
//    }
//
//    @Test
//    public void testListProblem() {
//        // 准备测试数据
//        ProblemQueryRequest request = new ProblemQueryRequest();
//        request.setCurrent(1);
//        request.setPageSize(10);
//        request.setTitle("题目");
//        request.setType("program");
//        request.setDifficulty("MEDIUM");
//        request.setTag("算法");
//        request.setJobType("开发");
//
//        // 执行测试
//        Page<ProblemVO> problemPage = problemService.listProblem(request);
//
//        // 验证结果
//        assertNotNull(problemPage);
//        assertTrue(problemPage.getRecords().size() > 0);
//        assertTrue(problemPage.getTotal() > 0);
//    }
//
//    @Test
//    public void testGetProblemDetail() {
//        // 执行测试
//        ProblemDTO problemDTO = problemService.getProblemDetail(1L, 1L);
//
//        // 验证结果
//        assertNotNull(problemDTO);
//        assertEquals("更新后的题目", problemDTO.getTitle());
//        assertEquals("更新后的内容", problemDTO.getContent());
//        assertNotNull(problemDTO.getProblemDetail());
//        assertTrue(problemDTO.getProblemDetail().contains("更新后的描述"));
//    }
//
//    @Test
//    public void testValidateProblem() {
//        // 执行测试
//        Problem problem = problemService.validateProblem(1L, 1L);
//
//        // 验证结果
//        assertNotNull(problem);
//        assertEquals(1L, problem.getId());
//        assertEquals(1L, problem.getUserId());
//    }
//}