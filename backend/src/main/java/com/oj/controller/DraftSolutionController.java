//package com.oj.controller;
//
//import com.oj.common.BaseResponse;
//import com.oj.common.ResultUtils;
//import com.oj.model.entity.DraftSolution;
//import com.oj.model.entity.User;
//import com.oj.model.request.DraftSolutionRequest;
//import com.oj.service.DraftSolutionService;
//import com.oj.service.UserService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import javax.validation.Valid;
//
///**
// * 用户草稿接口
// */
//@RestController
//@RequestMapping("/draft")
//@Slf4j
//public class DraftSolutionController {
//
//    @Resource
//    private DraftSolutionService draftSolutionService;
//
//    @Resource
//    private UserService userService;
//
//    /**
//     * 保存草稿
//     * @param request 草稿请求
//     * @param httpServletRequest HTTP请求
//     * @return 是否保存成功
//     */
//    @PostMapping("/save")
//    public BaseResponse<Boolean> saveDraft(@RequestBody @Valid DraftSolutionRequest request,
//                                          HttpServletRequest httpServletRequest) {
//        User loginUser = userService.getLoginUser(httpServletRequest);
//        boolean result = draftSolutionService.saveDraft(loginUser.getId(), request);
//        return ResultUtils.success(result);
//    }
//
//    /**
//     * 获取草稿
//     * @param problemId 题目ID
//     * @param httpServletRequest HTTP请求
//     * @return 草稿内容
//     */
//    @GetMapping("/get")
//    public BaseResponse<DraftSolution> getDraft(@RequestParam Long problemId,
//                                               HttpServletRequest httpServletRequest) {
//        User loginUser = userService.getLoginUser(httpServletRequest);
//        DraftSolution draftSolution = draftSolutionService.getUserDraft(loginUser.getId(), problemId);
//        return ResultUtils.success(draftSolution);
//    }
//
//    /**
//     * 删除草稿
//     * @param problemId 题目ID
//     * @param httpServletRequest HTTP请求
//     * @return 是否删除成功
//     */
//    @DeleteMapping("/delete")
//    public BaseResponse<Boolean> deleteDraft(@RequestParam Long problemId,
//                                           HttpServletRequest httpServletRequest) {
//        User loginUser = userService.getLoginUser(httpServletRequest);
//        boolean result = draftSolutionService.deleteUserDraft(loginUser.getId(), problemId);
//        return ResultUtils.success(result);
//    }
//}