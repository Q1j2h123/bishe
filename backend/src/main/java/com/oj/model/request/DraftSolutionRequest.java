//package com.oj.model.request;
//
//import lombok.Data;
//
//import javax.validation.constraints.NotBlank;
//import javax.validation.constraints.NotNull;
//
///**
// * 草稿保存请求类
// */
//@Data
//public class DraftSolutionRequest {
//
//    /**
//     * 题目ID
//     */
//    @NotNull(message = "题目ID不能为空")
//    private Long problemId;
//
//    /**
//     * 题目类型: CHOICE, JUDGE, PROGRAM
//     */
//    @NotBlank(message = "题目类型不能为空")
//    private String type;
//
//    /**
//     * 草稿内容(选择题/判断题的选择或编程题的代码)
//     */
//    @NotBlank(message = "草稿内容不能为空")
//    private String content;
//
//    /**
//     * 编程语言(编程题适用)
//     */
//    private String language;
//}