package com.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.exception.BusinessException;
import com.oj.model.dto.QuestionBankDTO;
import com.oj.model.entity.User;
import com.oj.model.request.BankProblemRequest;
import com.oj.model.request.QuestionBankAddRequest;
import com.oj.model.request.QuestionBankQueryRequest;
import com.oj.model.request.QuestionBankUpdateRequest;
import com.oj.model.vo.QuestionBankVO;
import com.oj.service.QuestionBankService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static com.oj.constant.CommonConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/api/question-bank")
@Api(tags = "题库管理接口")
@Slf4j
public class QuestionBankController {

    @Resource
    private QuestionBankService questionBankService;

    @PostMapping("/add")
    @ApiOperation(value = "添加题库", notes = "创建一个新的题库，需要管理员权限")
    public BaseResponse<Long> addQuestionBank(@ApiParam(value = "题库信息", required = true) @RequestBody @Valid QuestionBankAddRequest request,
                                            HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        Long questionBankId = questionBankService.addQuestionBank(request, loginUser.getId());
        return ResultUtils.success(questionBankId);
    }

    @PostMapping("/delete/{id}")
    @ApiOperation(value = "删除题库", notes = "根据id删除题库，需要管理员权限")
    public BaseResponse<Boolean> deleteQuestionBank(@ApiParam(value = "题库ID", required = true) @PathVariable("id") Long id,
                                                  HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = questionBankService.deleteQuestionBank(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新题库", notes = "更新已存在的题库信息，需要管理员权限")
    public BaseResponse<Boolean> updateQuestionBank(@ApiParam(value = "题库更新信息", required = true) @RequestBody @Valid QuestionBankUpdateRequest request,
                                                  HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = questionBankService.updateQuestionBank(request, loginUser.getId());
        return ResultUtils.success(result);
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "获取题库信息", notes = "根据id获取题库的基本信息")
    public BaseResponse<QuestionBankVO> getQuestionBankById(@ApiParam(value = "题库ID", required = true) @PathVariable("id") Long id,
                                                           HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        QuestionBankVO questionBankVO = questionBankService.getQuestionBankById(id, loginUser.getId());
        return ResultUtils.success(questionBankVO);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "获取题库详情", notes = "根据id获取题库的详细信息，包括题目列表等")
    public BaseResponse<QuestionBankVO> getQuestionBankDetail(@ApiParam(value = "题库ID", required = true) @PathVariable("id") Long id,
                                                            HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        QuestionBankDTO questionBankDTO = questionBankService.getQuestionBankDetail(id, loginUser.getId());
        QuestionBankVO questionBankVO = questionBankService.dtoToVO(questionBankDTO);
        return ResultUtils.success(questionBankVO);
    }

    @PostMapping("/list")
    @ApiOperation(value = "获取题库列表", notes = "分页获取题库列表，支持多种查询条件")
    public BaseResponse<Page<QuestionBankVO>> listQuestionBank(@ApiParam(value = "查询条件", required = true) @RequestBody QuestionBankQueryRequest request) {
        Page<QuestionBankVO> questionBankPage = questionBankService.listQuestionBank(request);
        return ResultUtils.success(questionBankPage);
    }

    @PostMapping("/list/my")
    @ApiOperation(value = "获取我的题库列表", notes = "分页获取当前用户创建的题库列表")
    public BaseResponse<Page<QuestionBankVO>> listMyQuestionBank(@ApiParam(value = "查询条件", required = true) @RequestBody QuestionBankQueryRequest request,
                                                                HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        request.setUserId(loginUser.getId());
        Page<QuestionBankVO> questionBankPage = questionBankService.listQuestionBank(request);
        return ResultUtils.success(questionBankPage);
    }

    @PostMapping("/problem/add")
    @ApiOperation(value = "添加题目到题库", notes = "向指定题库添加题目，需要管理员权限")
    public BaseResponse<Boolean> addProblemToBank(@ApiParam(value = "题目信息", required = true) @RequestBody @Valid BankProblemRequest request,
            HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = questionBankService.addProblemToBank(request.getBankId(),
                request.getProblemId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/problem/remove")
    @ApiOperation(value = "从题库移除题目", notes = "从指定题库移除题目，需要管理员权限")
    public BaseResponse<Boolean> removeProblemFromBank(@ApiParam(value = "题目信息", required = true) @RequestBody @Valid BankProblemRequest request,
            HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = questionBankService.removeProblemFromBank(request.getBankId(),
                request.getProblemId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    @GetMapping("/problem/list/{bankId}")
    @ApiOperation(value = "获取题库题目列表", notes = "获取指定题库中的所有题目ID")
    public BaseResponse<List<Long>> getProblemIdsByBankId(@ApiParam(value = "题库ID", required = true) @PathVariable("bankId") Long bankId) {
        List<Long> problemIds = questionBankService.getProblemIdsByBankId(bankId);
        return ResultUtils.success(problemIds);
    }

    private User getLoginUser(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return loginUser;
    }
}