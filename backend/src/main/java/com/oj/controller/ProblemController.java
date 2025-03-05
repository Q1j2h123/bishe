package com.oj.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.exception.BusinessException;
import com.oj.model.dto.*;
import com.oj.model.entity.Problem;
import com.oj.service.ProblemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 题目管理接口
 */
@RestController
@RequestMapping("/api/problem")
@Api(tags = "题目管理接口")
public class ProblemController {

    @Resource
    private ProblemService problemService;

    @PostMapping("/add")
    @ApiOperation("创建题目")
    public BaseResponse<Long> addProblem(@RequestBody ProblemAddRequest problemAddRequest) {
        if (problemAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long problemId = problemService.addProblem(problemAddRequest);
        return ResultUtils.success(problemId);
    }

    @PostMapping("/delete")
    @ApiOperation("删除题目")
    public BaseResponse<Boolean> deleteProblem(@RequestParam("id") long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = problemService.deleteProblem(id);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @ApiOperation("更新题目")
    public BaseResponse<Boolean> updateProblem(@RequestBody ProblemUpdateRequest problemUpdateRequest) {
        if (problemUpdateRequest == null || problemUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = problemService.updateProblem(problemUpdateRequest);
        return ResultUtils.success(result);
    }

    @GetMapping("/get")
    @ApiOperation("根据ID获取题目")
    public BaseResponse<Problem> getProblemById(@RequestParam("id") long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem problem = problemService.getProblemById(id);
        return ResultUtils.success(problem);
    }

    @GetMapping("/detail")
    @ApiOperation("获取题目详情")
    public BaseResponse<ProblemExcelDTO> getProblemDetail(@RequestParam("id") long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProblemExcelDTO problemDetail = problemService.getProblemDetail(id);
        return ResultUtils.success(problemDetail);
    }

    @PostMapping("/list/page")
    @ApiOperation("分页获取题目列表")
    public BaseResponse<Page<Problem>> listProblemByPage(@RequestBody ProblemQueryRequest problemQueryRequest) {
        if (problemQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Problem> problemPage = problemService.listProblemByPage(problemQueryRequest);
        return ResultUtils.success(problemPage);
    }

    @PostMapping("/import")
    @ApiOperation("导入题目")
    public BaseResponse<String> importProblems(@ApiParam("Excel文件") @RequestParam("file") MultipartFile file) {
        if (file == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        try {
            // 这里需要先读取Excel文件，转换为List<ProblemExcelDTO>
            List<ProblemExcelDTO> problemList = EasyExcel.read(file.getInputStream())
                    .head(ProblemExcelDTO.class)
                    .sheet()
                    .doReadSync();
            String result = problemService.importProblems(problemList);
            return ResultUtils.success(result);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读取失败");
        }
    }

    @GetMapping("/export")
    @ApiOperation("导出题目")
    public void exportProblems(HttpServletResponse response) {
        try {
            problemService.exportProblems(response);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件导出失败");
        }
    }

    @GetMapping("/search")
    @ApiOperation("搜索题目")
    public BaseResponse<List<Problem>> searchProblems(
            @ApiParam("标题") @RequestParam(required = false) String title,
            @ApiParam("内容") @RequestParam(required = false) String content,
            @ApiParam("类型") @RequestParam(required = false) String type,
            @ApiParam("岗位类型") @RequestParam(required = false) String jobType,
            @ApiParam("难度") @RequestParam(required = false) String difficulty,
            @ApiParam("标签") @RequestParam(required = false) String tags,
            @ApiParam("排序字段") @RequestParam(required = false) String sortField,
            @ApiParam("排序方式") @RequestParam(required = false) String sortOrder) {
        List<Problem> problems = problemService.searchProblems(title, content, type, jobType, difficulty, tags, sortField, sortOrder);
        return ResultUtils.success(problems);
    }
} 