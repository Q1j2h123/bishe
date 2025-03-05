package com.oj.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.oj.model.dto.ProblemExcelDTO;
import com.oj.model.entity.Problem;
import com.oj.service.ProblemService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目导入监听器
 */
@Slf4j
public class ProblemImportListener extends AnalysisEventListener<ProblemExcelDTO> {

    private final List<Problem> list = new ArrayList<>();
    private final ProblemService problemService;

    public ProblemImportListener(ProblemService problemService) {
        this.problemService = problemService;
    }

    @Override
    public void invoke(ProblemExcelDTO data, AnalysisContext context) {
        if (data == null) {
            log.warn("跳过空行");
            return;
        }

        try {
            Problem problem = new Problem();
            problem.setTitle(data.getTitle());
            problem.setContent(data.getContent());
            problem.setType(data.getType());
            problem.setJobType(data.getJobType());
            problem.setTags(data.getTags());
            problem.setAnswer(data.getAnswer());
            problem.setDifficulty(data.getDifficulty());
            list.add(problem);
        } catch (Exception e) {
            log.error("解析Excel行数据失败：{}", data, e);
            throw new RuntimeException("解析Excel行数据失败：" + e.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (list.isEmpty()) {
            log.warn("没有数据需要导入");
            return;
        }

        try {
            log.info("{}条数据，开始存储数据库！", list.size());
            problemService.saveBatch(list);
            log.info("数据导入成功！");
        } catch (Exception e) {
            log.error("数据导入失败", e);
            throw new RuntimeException("数据导入失败：" + e.getMessage());
        }
    }
} 