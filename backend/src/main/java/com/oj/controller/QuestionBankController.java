package com.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.exception.BusinessException;
import com.oj.model.entity.User;
import com.oj.model.request.BankProblemRequest;
import com.oj.model.request.QuestionBankAddRequest;
import com.oj.model.request.QuestionBankQueryRequest;
import com.oj.model.request.QuestionBankUpdateRequest;
import com.oj.model.vo.QuestionBankVO;
import com.oj.service.QuestionBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static com.oj.constant.CommonConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/api/question-bank")
@Tag(name = "题库接口", description = "题库管理相关接口")
@ApiSupport(author = "OJ System")
@Slf4j
public class QuestionBankController {

    @Resource
    private QuestionBankService questionBankService;

    @PostMapping("/add")
    @Operation(summary = "创建题库", description = "创建一个新的题库")
    @ApiOperationSupport(order = 1)
    public BaseResponse<Long> addQuestionBank(@RequestBody @Valid QuestionBankAddRequest request,
            HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        Long bankId = questionBankService.addQuestionBank(request, loginUser.getId());
        return ResultUtils.success(bankId);
    }

    @PostMapping("/delete/{id}")
    @Operation(summary = "删除题库", description = "根据id删除题库")
    @ApiOperationSupport(order = 2)
    public BaseResponse<Boolean> deleteQuestionBank(@PathVariable("id") Long id,
            HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = questionBankService.deleteQuestionBank(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @Operation(summary = "更新题库", description = "更新已存在的题库")
    @ApiOperationSupport(order = 3)
    public BaseResponse<Boolean> updateQuestionBank(@RequestBody @Valid QuestionBankUpdateRequest request,
            HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = questionBankService.updateQuestionBank(request, loginUser.getId());
        return ResultUtils.success(result);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取题库信息", description = "根据id获取题库信息")
    @ApiOperationSupport(order = 4)
    public BaseResponse<QuestionBankVO> getQuestionBankById(@PathVariable("id") Long id,
            HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        QuestionBankVO bankVO = questionBankService.getQuestionBankById(id, loginUser.getId());
        return ResultUtils.success(bankVO);
    }

    @PostMapping("/list")
    @Operation(summary = "获取题库列表", description = "分页获取题库列表")
    @ApiOperationSupport(order = 5)
    public BaseResponse<Page<QuestionBankVO>> listQuestionBank(@RequestBody QuestionBankQueryRequest request) {
        Page<QuestionBankVO> bankPage = questionBankService.listQuestionBank(request);
        return ResultUtils.success(bankPage);
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的题库列表", description = "获取当前用户创建的题库列表")
    @ApiOperationSupport(order = 6)
    public BaseResponse<List<QuestionBankVO>> getMyQuestionBanks(HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        List<QuestionBankVO> bankList = questionBankService.getUserQuestionBanks(loginUser.getId());
        return ResultUtils.success(bankList);
    }

    @PostMapping("/problem/add")
    @Operation(summary = "添加题目到题库", description = "向指定题库添加题目")
    @ApiOperationSupport(order = 7)
    public BaseResponse<Boolean> addProblemToBank(@RequestBody @Valid BankProblemRequest request,
            HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = questionBankService.addProblemToBank(request.getBankId(),
                request.getProblemId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/problem/remove")
    @Operation(summary = "从题库移除题目", description = "从指定题库移除题目")
    @ApiOperationSupport(order = 8)
    public BaseResponse<Boolean> removeProblemFromBank(@RequestBody @Valid BankProblemRequest request,
            HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        boolean result = questionBankService.removeProblemFromBank(request.getBankId(),
                request.getProblemId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    @GetMapping("/problem/list/{bankId}")
    @Operation(summary = "获取题库题目列表", description = "获取指定题库中的所有题目ID")
    @ApiOperationSupport(order = 9)
    public BaseResponse<List<Long>> getProblemIdsByBankId(@PathVariable("bankId") Long bankId) {
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