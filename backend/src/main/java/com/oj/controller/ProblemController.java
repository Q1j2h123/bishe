package com.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.exception.BusinessException;
import com.oj.model.dto.ProblemDTO;
import com.oj.model.entity.User;
import com.oj.model.request.ProblemAddRequest;
import com.oj.model.request.ProblemQueryRequest;
import com.oj.model.request.ProblemUpdateRequest;
import com.oj.model.vo.ProblemVO;
import com.oj.service.ProblemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.oj.constant.CommonConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/api/problem")
@Api(tags = "题目接口", value = "题目管理相关接口")
@ApiSupport(author = "OJ System")
@Slf4j
public class ProblemController {

    @Resource
    private ProblemService problemService;

    @PostMapping("/add")
    @ApiOperation(value = "创建题目", notes = "创建一个新的题目")
    @ApiOperationSupport(order = 1)
    public BaseResponse<Long> addProblem(@RequestBody @Valid ProblemAddRequest request,
                                         HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        Long problemId = problemService.addProblem(request, loginUser.getId());
        return ResultUtils.success(problemId);
    }

    @PostMapping("/delete/{id}")
    @ApiOperation(value = "删除题目", notes = "根据id删除题目")
    @ApiOperationSupport(order = 2)
    public BaseResponse<Boolean> deleteProblem(@PathVariable("id") Long id,
                                               HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = problemService.deleteProblem(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新题目", notes = "更新已存在的题目")
    @ApiOperationSupport(order = 3)
    public BaseResponse<Boolean> updateProblem(@RequestBody @Valid ProblemUpdateRequest request,
                                               HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = problemService.updateProblem(request, loginUser.getId());
        return ResultUtils.success(result);
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "获取题目信息", notes = "根据id获取题目基本信息")
    @ApiOperationSupport(order = 4)
    public BaseResponse<ProblemVO> getProblemById(@PathVariable("id") Long id,
                                                  HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        ProblemVO problemVO = problemService.getProblemById(id, loginUser.getId());
        return ResultUtils.success(problemVO);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "获取题目详细信息", notes = "根据id获取题目的详细信息")
    @ApiOperationSupport(order = 5)
    public BaseResponse<ProblemVO> getProblemDetail(@PathVariable("id") Long id,
                                                    HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        ProblemDTO problemDTO = problemService.getProblemDetail(id, loginUser.getId());
        ProblemVO problemVO = problemService.dtoToVO(problemDTO);
        return ResultUtils.success(problemVO);
    }

    @PostMapping("/list")
    @ApiOperation(value = "获取题目列表", notes = "分页获取题目列表")
    @ApiOperationSupport(order = 6)
    public BaseResponse<Page<ProblemVO>> listProblem(@RequestBody ProblemQueryRequest request) {
        Page<ProblemVO> problemPage = problemService.listProblem(request);
        return ResultUtils.success(problemPage);
    }

    @PostMapping("/list/my")
    @ApiOperation(value = "获取我的题目列表", notes = "分页获取当前用户创建的题目列表")
    @ApiOperationSupport(order = 7)
    public BaseResponse<Page<ProblemVO>> listMyProblem(@RequestBody ProblemQueryRequest request,
                                                       HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        request.setUserId(loginUser.getId());
        Page<ProblemVO> problemPage = problemService.listProblem(request);
        return ResultUtils.success(problemPage);
    }

    /**
     * 获取当前登录用户
     */
    private User getLoginUser(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return loginUser;
    }
}