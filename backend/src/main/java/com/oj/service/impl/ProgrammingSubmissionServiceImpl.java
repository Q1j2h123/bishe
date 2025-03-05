package com.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.common.ErrorCode;
import com.oj.exception.BusinessException;
import com.oj.mapper.ProgrammingSubmissionMapper;
import com.oj.model.dto.SubmissionRequest;
import com.oj.model.entity.ProgrammingSubmission;
import com.oj.service.ProgrammingSubmissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 编程题提交记录服务实现类
 */
@Service
public class ProgrammingSubmissionServiceImpl extends ServiceImpl<ProgrammingSubmissionMapper, ProgrammingSubmission>
        implements ProgrammingSubmissionService {

    @Resource
    private ProgrammingSubmissionMapper programmingSubmissionMapper;

    @Override
    public String submit(SubmissionRequest submissionRequest) {
        if (submissionRequest == null || submissionRequest.getProblemId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        ProgrammingSubmission submission = new ProgrammingSubmission();
        submission.setProblemId(submissionRequest.getProblemId());
        submission.setUserId(1L); // TODO: 从登录用户中获取
        submission.setLanguage(submissionRequest.getLanguage());
        submission.setCode(submissionRequest.getAnswer());
        submission.setStatus("PENDING"); // 初始状态为等待判题
        submission.setSubmitTime(LocalDateTime.now());

        // 保存提交记录
        boolean success = this.save(submission);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提交失败");
        }

        // TODO: 异步执行判题
        // 这里需要实现判题逻辑，包括：
        // 1. 编译代码
        // 2. 运行测试用例
        // 3. 更新提交状态和结果

        return "提交成功，正在判题中";
    }

    @Override
    public List<ProgrammingSubmission> getUserProblemSubmissions(Long userId, Long problemId) {
        return programmingSubmissionMapper.getUserProblemSubmissions(userId, problemId);
    }

    @Override
    public List<ProgrammingSubmission> getUserSubmissions(Long userId) {
        return programmingSubmissionMapper.getUserSubmissions(userId);
    }

    @Override
    public String getProblemAcceptRate(Long problemId) {
        return programmingSubmissionMapper.getProblemAcceptRate(problemId);
    }
} 