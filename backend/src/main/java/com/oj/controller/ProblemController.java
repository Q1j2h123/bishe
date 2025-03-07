package com.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.annotation.AuthCheck;
import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.common.UserContext;
import com.oj.constant.UserConstant;
import com.oj.exception.BusinessException;
import com.oj.model.dto.ProblemDTO;
import com.oj.model.entity.User;
import com.oj.model.request.ChoiceProblemAddRequest;
import com.oj.model.request.JudgeProblemAddRequest;
import com.oj.model.request.ProgrammingProblemAddRequest;
import com.oj.model.request.ProblemAddRequest;
import com.oj.model.request.ProblemQueryRequest;
import com.oj.model.request.ProblemUpdateRequest;
import com.oj.model.request.ChoiceProblemUpdateRequest;
import com.oj.model.request.JudgeProblemUpdateRequest;
import com.oj.model.request.ProgrammingProblemUpdateRequest;
import com.oj.model.vo.ProblemVO;
import com.oj.service.ProblemService;
import com.oj.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/problem")
@Api(tags = "题目管理接口")
@Slf4j
@Validated
public class ProblemController {

    @Resource
    private ProblemService problemService;
    
    @Resource
    private UserService userService;


    @PostMapping("/choice/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "删除选择题", notes = "删除选择题，需要管理员权限")
    public BaseResponse<Boolean> deleteChoiceProblem(@RequestParam Long id, HttpServletRequest request) {
    if (id == null || id <= 0) {
    throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    Long userId = (Long) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    return ResultUtils.success(problemService.deleteProblem(id, userId));
    }
    @PostMapping("/judge/delete")
    @ApiOperation(value = "删除判断题", notes = "删除判断题，需要管理员权限")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
public BaseResponse<Boolean> deleteJudgeProblem(@RequestParam Long id, HttpServletRequest request) {
if (id == null || id <= 0) {
throw new BusinessException(ErrorCode.PARAMS_ERROR);
}
Long userId = (Long) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
return ResultUtils.success(problemService.deleteProblem(id, userId));
}
@PostMapping("/programming/delete")
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
@ApiOperation(value = "删除编程题", notes = "删除编程题，需要管理员权限")
public BaseResponse<Boolean> deleteProgrammingProblem(@RequestParam Long id, HttpServletRequest request) {
    if (id == null || id <= 0) {
    throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    Long userId = (Long) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    return ResultUtils.success(problemService.deleteProblem(id, userId));
}


    @GetMapping("/get/{id}")
    @ApiOperation(value = "获取题目信息", notes = "根据id获取题目的基本信息")
    public BaseResponse<ProblemVO> getProblemById(@ApiParam(value = "题目ID", required = true) @PathVariable("id") Long id,
                                                HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        ProblemVO problemVO = problemService.getProblemById(id, loginUser.getId());
        return ResultUtils.success(problemVO);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "获取题目详情", notes = "根据id获取题目的详细信息，包括描述、示例等")
    public BaseResponse<ProblemVO> getProblemDetail(@ApiParam(value = "题目ID", required = true) @PathVariable("id") Long id,
                                                  HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        ProblemDTO problemDTO = problemService.getProblemDetail(id, loginUser.getId());
        ProblemVO problemVO = problemService.dtoToVO(problemDTO);
        return ResultUtils.success(problemVO);
    }

    @PostMapping("/list")
    @ApiOperation(value = "获取题目列表", notes = "分页获取题目列表，支持多种查询条件")
    public BaseResponse<Page<ProblemVO>> listProblem(@ApiParam(value = "查询条件", required = true) @RequestBody ProblemQueryRequest request) {
        Page<ProblemVO> problemPage = problemService.listProblem(request);
        return ResultUtils.success(problemPage);
    }

    @PostMapping("/list/my")
    @ApiOperation(value = "获取我的题目列表", notes = "分页获取当前用户创建的题目列表")
    public BaseResponse<Page<ProblemVO>> listMyProblem(@ApiParam(value = "查询条件", required = true) @RequestBody ProblemQueryRequest request,
                                                     HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        request.setUserId(loginUser.getId());
        Page<ProblemVO> problemPage = problemService.listProblem(request);
        return ResultUtils.success(problemPage);
    }

    @PostMapping("/choice/add")
    @ApiOperation(value = "添加选择题", notes = "创建一个新的选择题，需要管理员权限")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addChoiceProblem(@ApiParam(value = "选择题信息", required = true) @RequestBody @Valid ChoiceProblemAddRequest request,
                                             HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        Long problemId = problemService.addChoiceProblem(request, loginUser.getId());
        return ResultUtils.success(problemId);
    }

    @PostMapping("/judge/add")
    @ApiOperation(value = "添加判断题", notes = "需要管理员权限")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addJudgeProblem(@RequestBody @Validated JudgeProblemAddRequest judgeAddRequest,
                                            HttpServletRequest request) {
        if (judgeAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long problemId = problemService.addJudgeProblem(judgeAddRequest, loginUser.getId());
        return ResultUtils.success(problemId);
    }

    @PostMapping("/programming/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "添加编程题", notes = "创建一个新的编程题，需要管理员权限")
    public BaseResponse<Long> addProgrammingProblem(@ApiParam(value = "编程题信息", required = true) @RequestBody @Valid ProgrammingProblemAddRequest request,
                                                  HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        Long problemId = problemService.addProgrammingProblem(request, loginUser.getId());
        return ResultUtils.success(problemId);
    }

    @PostMapping("/choice/update")
    @ApiOperation(value = "更新选择题", notes = "更新已存在的选择题，需要管理员权限")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChoiceProblem(@ApiParam(value = "选择题更新信息", required = true) @RequestBody @Valid ChoiceProblemUpdateRequest request,
                                                   HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = problemService.updateChoiceProblem(request, loginUser.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/judge/update")
    @ApiOperation(value = "更新判断题", notes = "更新已存在的判断题，需要管理员权限")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateJudgeProblem(@ApiParam(value = "判断题更新信息", required = true) @RequestBody @Valid JudgeProblemUpdateRequest request,
                                                  HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = problemService.updateJudgeProblem(request, loginUser.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/programming/update")
    @ApiOperation(value = "更新编程题", notes = "更新已存在的编程题，需要管理员权限")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateProgrammingProblem(@ApiParam(value = "编程题更新信息", required = true) @RequestBody @Valid ProgrammingProblemUpdateRequest request,
                                                        HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = problemService.updateProgrammingProblem(request, loginUser.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/batch")
    @ApiOperation("批量获取题目")
    public BaseResponse<List<ProblemVO>> getProblemsByIds(@RequestBody List<Long> problemIds) {
        List<ProblemVO> problems = problemService.getProblemsByIds(problemIds);
        return ResultUtils.success(problems);
    }

    @GetMapping("/user/{userId}")
    @ApiOperation("获取用户的题目列表")
    public BaseResponse<List<ProblemVO>> getProblemsByUserId(@PathVariable Long userId) {
        List<ProblemVO> problems = problemService.getProblemsByUserId(userId);
        return ResultUtils.success(problems);
    }

    @GetMapping("/search")
    @ApiOperation("搜索题目")
    public BaseResponse<List<ProblemVO>> searchProblems(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "搜索关键词不能为空");
        }
        List<ProblemVO> problems = problemService.searchProblems(keyword);
        return ResultUtils.success(problems);
    }

//    /**
//     * 获取随机一题
//     */
//    @GetMapping("/random")
//    @ApiOperation("获取随机一题")
//    public BaseResponse<ProblemVO> getRandomProblem() {
//        Long userId = UserContext.getUserId();
//        ProblemVO problemVO = problemService.getRandomProblem(userId);
//        return ResultUtils.success(problemVO);
//    }
//
//    /**
//     * 获取每日一题
//     */
//    @GetMapping("/daily")
//    @ApiOperation("获取每日一题")
//    public BaseResponse<ProblemVO> getDailyProblem() {
//        Long userId = UserContext.getUserId();
//        ProblemVO problemVO = problemService.getDailyProblem(userId);
//        return ResultUtils.success(problemVO);
//    }

    /**
     * 获取当前登录用户
     */
    private User getLoginUser(HttpServletRequest request) {
        User loginUser = UserContext.getUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return loginUser;
    }
}