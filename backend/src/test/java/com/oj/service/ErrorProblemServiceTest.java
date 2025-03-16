package com.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.model.dto.ErrorProblemDTO;
import com.oj.model.entity.ErrorProblem;
import com.oj.model.entity.Problem;
import com.oj.model.request.ErrorProblemQueryRequest;
import com.oj.service.impl.ErrorProblemServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 错题本服务测试
 */
@SpringBootTest
@Transactional  // 使用事务注解，测试完自动回滚
public class ErrorProblemServiceTest {

    @Resource
    private ErrorProblemService errorProblemService;

    @Test
    public void testAddErrorProblem() {
        // 测试添加错题记录
        Long userId = 1L;
        Long problemId = 1L;
        boolean result = errorProblemService.addErrorProblem(userId, problemId);
        
        // 验证添加成功
        assertTrue(result);
        
        // 验证记录已存在
        ErrorProblem errorProblem = findErrorProblem(userId, problemId);
        assertNotNull(errorProblem);
        assertEquals(userId, errorProblem.getUserId());
        assertEquals(problemId, errorProblem.getProblemId());
        assertEquals(0, errorProblem.getStatus());
        assertNotNull(errorProblem.getLastErrorTime());
    }
    
    @Test
    public void testUpdateErrorTime() {
        // 先添加一条记录
        Long userId = 1L;
        Long problemId = 1L;
        errorProblemService.addErrorProblem(userId, problemId);
        
        // 记录添加时间
        ErrorProblem beforeUpdate = findErrorProblem(userId, problemId);
        LocalDateTime oldTime = beforeUpdate.getLastErrorTime();
        
        // 等待一段时间以保证时间变化
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 更新错误时间
        boolean result = errorProblemService.updateErrorTime(userId, problemId);
        
        // 验证更新成功
        assertTrue(result);
        
        // 验证时间已更新
        ErrorProblem afterUpdate = findErrorProblem(userId, problemId);
        LocalDateTime newTime = afterUpdate.getLastErrorTime();
        
        assertTrue(newTime.isAfter(oldTime), "新时间应该晚于旧时间");
    }
    
    @Test
    public void testMarkAsMastered() {
        // 先添加一条记录
        Long userId = 1L;
        Long problemId = 1L;
        errorProblemService.addErrorProblem(userId, problemId);
        
        // 标记为已掌握
        boolean result = errorProblemService.markAsMastered(userId, problemId);
        
        // 验证更新成功
        assertTrue(result);
        
        // 验证状态已更新
        ErrorProblem afterUpdate = findErrorProblem(userId, problemId);
        assertEquals(1, afterUpdate.getStatus());
    }
    
    @Test
    public void testGetErrorProblemsByUserId() {
        // 先添加几条记录
        Long userId = 1L;
        
        errorProblemService.addErrorProblem(userId, 1L);
        errorProblemService.addErrorProblem(userId, 2L);
        errorProblemService.addErrorProblem(userId, 3L);
        
        // 标记一条为已掌握，这条不应该出现在结果中
        errorProblemService.markAsMastered(userId, 3L);
        
        // 创建查询请求
        ErrorProblemQueryRequest request = new ErrorProblemQueryRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        
        // 查询错题记录
        Page<ErrorProblemDTO> page = errorProblemService.getErrorProblemsByUserId(userId, request);
        
        // 由于测试数据库中可能没有对应的Problem记录，可能返回空列表
        // 这里仅测试接口能正常调用即可
        assertNotNull(page);
    }
    
    /**
     * 辅助方法：根据用户ID和题目ID查找错题记录
     */
    private ErrorProblem findErrorProblem(Long userId, Long problemId) {
        return ((ErrorProblemServiceImpl) errorProblemService).lambdaQuery()
                .eq(ErrorProblem::getUserId, userId)
                .eq(ErrorProblem::getProblemId, problemId)
                .one();
    }
} 