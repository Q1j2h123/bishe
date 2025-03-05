package com.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.exception.BusinessException;
import com.oj.model.dto.DeleteRequest;
import com.oj.model.dto.QuestionBankAddRequest;
import com.oj.model.dto.QuestionBankQueryRequest;
import com.oj.model.entity.QuestionBank;
import com.oj.model.entity.User;
import com.oj.service.QuestionBankService;
import com.oj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题库接口
 */
@RestController
@RequestMapping("/questionBank")
@Slf4j
public class QuestionBankController {

    @Resource
    private QuestionBankService questionBankService;

    @Resource
    private UserService userService;

    /**
     * 创建题库
     *
     * @param questionBankAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestionBank(@RequestBody QuestionBankAddRequest questionBankAddRequest,
                                            HttpServletRequest request) {
        if (questionBankAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankAddRequest, questionBank);
        // 校验
        questionBankService.validQuestionBank(questionBank, true);
        User loginUser = userService.getLoginUser(request);
        long result = questionBankService.addQuestionBank(questionBankAddRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除题库
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionBank(@RequestBody DeleteRequest deleteRequest,
                                                  HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionBank oldQuestionBank = questionBankService.getById(id);
        if (oldQuestionBank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }
        // 仅管理员可删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionBankService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 更新题库
     *
     * @param questionBank
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateQuestionBank(@RequestBody QuestionBank questionBank,
                                                  HttpServletRequest request) {
        if (questionBank == null || questionBank.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 参数校验
        questionBankService.validQuestionBank(questionBank, false);
        User user = userService.getLoginUser(request);
        long id = questionBank.getId();
        // 判断是否存在
        QuestionBank oldQuestionBank = questionBankService.getById(id);
        if (oldQuestionBank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }
        // 仅管理员可修改
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionBankService.updateById(questionBank);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取题库
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<QuestionBank> getQuestionBankById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionBank questionBank = questionBankService.getById(id);
        return ResultUtils.success(questionBank);
    }

    /**
     * 分页获取题库列表
     *
     * @param questionBankQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionBank>> listQuestionBankByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                                 HttpServletRequest request) {
        long current = questionBankQueryRequest.getCurrent();
        long pageSize = questionBankQueryRequest.getPageSize();
        // 限制爬虫
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<QuestionBank> questionBankPage = questionBankService.page(new Page<>(current, pageSize),
                questionBankService.getQueryWrapper(questionBankQueryRequest));
        return ResultUtils.success(questionBankPage);
    }
} 