package com.oj.controller;

import com.oj.common.BaseResponse;
import com.oj.common.ErrorCode;
import com.oj.common.ResultUtils;
import com.oj.exception.BusinessException;
import com.oj.model.dto.SubmissionRequest;
import com.oj.model.entity.ChoiceJudgeSubmission;
import com.oj.model.entity.ProgrammingSubmission;
import com.oj.service.ChoiceJudgeSubmissionService;
import com.oj.service.ProgrammingSubmissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 提交记录接口
 */
@RestController
@RequestMapping("/api/submission")
@Api(tags = "提交记录接口")
public class SubmissionController {

    @Resource
    private ProgrammingSubmissionService programmingSubmissionService;

    @Resource
    private ChoiceJudgeSubmissionService choiceJudgeSubmissionService;

    @PostMapping("/submit")
    @ApiOperation("提交答案")
    public BaseResponse<String> submitAnswer(@RequestBody SubmissionRequest submissionRequest) {
        if (submissionRequest == null || submissionRequest.getProblemId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String result = submissionRequest.getType().equals("PROGRAM") ?
                programmingSubmissionService.submit(submissionRequest) :
                choiceJudgeSubmissionService.submit(submissionRequest);
        return ResultUtils.success(result);
    }

    @GetMapping("/programming/list")
    @ApiOperation("获取用户的编程题提交记录")
    public BaseResponse<List<ProgrammingSubmission>> listProgrammingSubmissions(
            @ApiParam("用户ID") @RequestParam Long userId,
            @ApiParam("题目ID") @RequestParam(required = false) Long problemId) {
        List<ProgrammingSubmission> submissions = problemId == null ?
                programmingSubmissionService.getUserSubmissions(userId) :
                programmingSubmissionService.getUserProblemSubmissions(userId, problemId);
        return ResultUtils.success(submissions);
    }

    @GetMapping("/choice-judge/list")
    @ApiOperation("获取用户的选择判断题提交记录")
    public BaseResponse<List<ChoiceJudgeSubmission>> listChoiceJudgeSubmissions(
            @ApiParam("用户ID") @RequestParam Long userId,
            @ApiParam("题目ID") @RequestParam(required = false) Long problemId) {
        List<ChoiceJudgeSubmission> submissions = problemId == null ?
                choiceJudgeSubmissionService.getUserSubmissions(userId) :
                choiceJudgeSubmissionService.getUserProblemSubmissions(userId, problemId);
        return ResultUtils.success(submissions);
    }

    @GetMapping("/programming/rate")
    @ApiOperation("获取编程题通过率")
    public BaseResponse<String> getProgrammingAcceptRate(@ApiParam("题目ID") @RequestParam Long problemId) {
        String rate = programmingSubmissionService.getProblemAcceptRate(problemId);
        return ResultUtils.success(rate);
    }

    @GetMapping("/choice-judge/rate")
    @ApiOperation("获取选择判断题通过率")
    public BaseResponse<String> getChoiceJudgeAcceptRate(@ApiParam("题目ID") @RequestParam Long problemId) {
        String rate = choiceJudgeSubmissionService.getProblemAcceptRate(problemId);
        return ResultUtils.success(rate);
    }
} 