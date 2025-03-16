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
import com.oj.model.request.*;

import com.oj.model.vo.ProblemVO;
import com.oj.service.ProblemService;
import com.oj.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
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
@PostMapping("/program/delete")
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
@ApiOperation(value = "删除编程题", notes = "删除编程题，需要管理员权限")
public BaseResponse<Boolean> deleteProgramProblem(@RequestParam Long id, HttpServletRequest request) {
    if (id == null || id <= 0) {
    throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    Long userId = (Long) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    return ResultUtils.success(problemService.deleteProblem(id, userId));
}


    @PostMapping("/choice/add")
    @ApiOperation(value = "添加选择题", notes = "创建一个新的选择题，需要管理员权限")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addChoiceProblem(@ApiParam(value = "选择题信息", required = true) @RequestBody @Valid ChoiceProblemAddRequest request,
                                             HttpServletRequest httpServletRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
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

    @PostMapping("/program/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "添加编程题", notes = "创建一个新的编程题，需要管理员权限")
    public BaseResponse<Long> addProgramProblem(@ApiParam(value = "编程题信息", required = true) @RequestBody @Valid ProgramProblemAddRequest request,
                                                  HttpServletRequest httpServletRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = getLoginUser(httpServletRequest);
        Long problemId = problemService.addProgramProblem(request, loginUser.getId());
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

    @PostMapping("/program/update")
    @ApiOperation(value = "更新编程题", notes = "更新已存在的编程题，需要管理员权限")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateProgramProblem(@ApiParam(value = "编程题更新信息", required = true) @RequestBody @Valid ProgramProblemUpdateRequest request,
                                                        HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = problemService.updateProgramProblem(request, loginUser.getId());
        return ResultUtils.success(result);
    }

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
    
    /**
     * 分页获取题目列表
     */
    @GetMapping("/list/page")
    @ApiOperation(value = "分页获取题目列表", notes = "支持多条件筛选")
    public BaseResponse<Page<ProblemVO>> listProblemByPage(ProblemQueryRequest problemQueryRequest, 
                                                         @RequestParam(required = false) String tag,
                                                         @RequestParam(required = false) String tagList,
                                                         HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = getLoginUser(request);
        long userId = loginUser.getId();
        
        // 处理标签参数 - 从tag或tagList中提取标签
        if (problemQueryRequest.getTags() == null) {
            problemQueryRequest.setTags(new ArrayList<>());
        }
        
        // 处理单个标签
        if (StringUtils.isNotBlank(tag)) {
            log.info("接收到单个标签查询参数: {}", tag);
            problemQueryRequest.getTags().add(tag);
        }
        
        // 处理多个标签
        if (StringUtils.isNotBlank(tagList)) {
            log.info("接收到标签列表查询参数: {}", tagList);
            String[] tagArray = tagList.split(",");
            for (String t : tagArray) {
                if (StringUtils.isNotBlank(t)) {
                    problemQueryRequest.getTags().add(t.trim());
                }
            }
        }
        
        if (!problemQueryRequest.getTags().isEmpty()) {
            log.info("处理后的标签列表: {}", problemQueryRequest.getTags());
        }
        
        // 分页查询
        Page<ProblemVO> problemVOPage = problemService.listProblemByPage(problemQueryRequest, userId);
        return ResultUtils.success(problemVOPage);
    }
    
    /**
     * 根据ID获取题目
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "获取题目信息", notes = "根据id获取题目的基本信息")//题目列表或概览展示
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
        try {
            User loginUser = userService.getLoginUser(httpServletRequest);
            // 传null作为userId，表明这是查看操作而非修改操作
            ProblemDTO problemDTO = problemService.getProblemDetail(id, null);
            ProblemVO problemVO = problemService.dtoToVO(problemDTO);
            return ResultUtils.success(problemVO);
        } catch (BusinessException e) {
            log.error("获取题目详情业务异常: {}", e.getMessage());
            return ResultUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取题目详情异常: ", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常，请联系管理员");
        }
    }
    
    /**
     * 批量获取题目
     */
    @PostMapping("/batch")
    @ApiOperation("批量获取题目")
    public BaseResponse<List<ProblemVO>> getProblemsByIds(@RequestBody List<Long> problemIds) {
        List<ProblemVO> problems = problemService.getProblemsByIds(problemIds);
        return ResultUtils.success(problems);
    }
    
    /**
     * 获取用户的题目列表
     */
    @GetMapping("/user/{userId}")
    @ApiOperation("获取用户的题目列表")
    public BaseResponse<List<ProblemVO>> getProblemsByUserId(@PathVariable Long userId) {
        List<ProblemVO> problems = problemService.getProblemsByUserId(userId);
        return ResultUtils.success(problems);
    }
    
    /**
     * 搜索题目
     */
    @GetMapping("/search")
    @ApiOperation("搜索题目")
    public BaseResponse<List<ProblemVO>> searchProblems(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "搜索关键词不能为空");
        }
        List<ProblemVO> problems = problemService.searchProblems(keyword);
        return ResultUtils.success(problems);
    }
    
    /**
     * 更新题目状态
     */
    @PostMapping("/status/update")
    @ApiOperation(value = "更新题目状态", notes = "更新题目的解决状态")
    public BaseResponse<Boolean> updateProblemStatus(@RequestParam Long problemId, @RequestParam String status, 
                                                  HttpServletRequest request) {
        if (problemId == null || problemId <= 0 || org.apache.commons.lang3.StringUtils.isBlank(status)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = getLoginUser(request);
        boolean result = problemService.updateProblemStatus(problemId, loginUser.getId(), status);
        return ResultUtils.success(result);
    }
    
    /**
     * 获取随机一题
     */
    @GetMapping("/random")
    @ApiOperation("获取随机一题")
    public BaseResponse<ProblemVO> getRandomProblem(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        ProblemVO problemVO = problemService.getRandomProblem(loginUser.getId());
        return ResultUtils.success(problemVO);
    }
    
    /**
     * 获取每日一题
     */
    @GetMapping("/daily")
    @ApiOperation("获取每日一题")
    public BaseResponse<ProblemVO> getDailyProblem(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        ProblemVO problemVO = problemService.getDailyProblem(loginUser.getId());
        return ResultUtils.success(problemVO);
    }

    /**
     * 高级搜索题目
     */
    @PostMapping("/search/advanced")
    @ApiOperation(value = "高级搜索题目", notes = "支持多条件复杂筛选")
    public BaseResponse<Page<ProblemVO>> searchProblemAdvanced(@RequestBody ProblemSearchRequest problemSearchRequest, 
                                                           HttpServletRequest request) {
        if (problemSearchRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = getLoginUser(request);
        Page<ProblemVO> problemVOPage = problemService.searchProblemAdvanced(problemSearchRequest, loginUser.getId());
        return ResultUtils.success(problemVOPage);
    }

    /**
     * 获取所有标签
     *
     * @return 所有标签列表
     */
    @GetMapping("/tags/all")
    public BaseResponse<List<String>> getAllTags() {
        List<String> tags = problemService.getAllTags();
        return ResultUtils.success(tags);
    }
}