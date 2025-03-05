package com.oj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.model.dto.ProblemAddRequest;
import com.oj.model.dto.ProblemQueryRequest;
import com.oj.model.dto.ProblemUpdateRequest;
import com.oj.model.entity.Problem;
import com.oj.model.dto.ProblemExcelDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 问题服务接口
 */
public interface ProblemService extends IService<Problem> {

    /**
     * 创建问题
     *
     * @param problemAddRequest
     * @return
     */
    long addProblem(ProblemAddRequest problemAddRequest);

    /**
     * 删除问题
     *
     * @param id
     * @return
     */
    boolean deleteProblem(long id);

    /**
     * 更新问题
     *
     * @param problemUpdateRequest
     * @return
     */
    boolean updateProblem(ProblemUpdateRequest problemUpdateRequest);

    /**
     * 根据 id 获取问题
     *
     * @param id
     * @return
     */
    Problem getProblemById(long id);

    /**
     * 分页获取问题列表
     *
     * @param problemQueryRequest
     * @return
     */
    Page<Problem> listProblemByPage(ProblemQueryRequest problemQueryRequest);

    /**
     * 获取查询条件
     *
     * @param problemQueryRequest
     * @return
     */
    QueryWrapper<Problem> getQueryWrapper(ProblemQueryRequest problemQueryRequest);

    /**
     * 校验问题
     *
     * @param problem
     * @param add
     */
    void validProblem(Problem problem, boolean add);

    /**
     * 导入题目
     *
     * @param problemList 题目列表
     * @return 导入结果
     */
    String importProblems(List<ProblemExcelDTO> problemList);

    /**
     * 导出题目
     *
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportProblems(HttpServletResponse response) throws IOException;

    /**
     * 创建题目
     *
     * @param problemDTO 题目信息
     * @return 题目ID
     */
    Long createProblem(ProblemExcelDTO problemDTO);

    /**
     * 更新题目
     *
     * @param id 题目ID
     * @param problemDTO 题目信息
     */
    void updateProblem(Long id, ProblemExcelDTO problemDTO);

    /**
     * 删除题目
     *
     * @param id 题目ID
     */
    void deleteProblem(Long id);

    /**
     * 获取题目详情
     *
     * @param id 题目ID
     * @return 题目信息
     */
    ProblemExcelDTO getProblemDetail(Long id);

    /**
     * 搜索题目
     *
     * @param title      标题
     * @param content    内容
     * @param type       类型
     * @param jobType    岗位类型
     * @param difficulty 难度
     * @param tags       标签
     * @param sortField  排序字段
     * @param sortOrder  排序方式
     * @return 题目列表
     */
    List<Problem> searchProblems(String title, String content, String type, String jobType, 
                                String difficulty, String tags, String sortField, String sortOrder);
} 