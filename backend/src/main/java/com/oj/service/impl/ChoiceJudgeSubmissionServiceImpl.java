package com.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.common.ErrorCode;
import com.oj.exception.BusinessException;
import com.oj.mapper.ChoiceJudgeSubmissionMapper;
import com.oj.mapper.ChoiceProblemMapper;
import com.oj.mapper.JudgeProblemMapper;
import com.oj.model.dto.SubmissionRequest;
import com.oj.model.entity.ChoiceJudgeSubmission;
import com.oj.model.entity.ChoiceProblem;
import com.oj.model.entity.JudgeProblem;
import com.oj.service.ChoiceJudgeSubmissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 选择判断题提交记录服务实现类
 */
@Service
public class ChoiceJudgeSubmissionServiceImpl extends ServiceImpl<ChoiceJudgeSubmissionMapper, ChoiceJudgeSubmission>
        implements ChoiceJudgeSubmissionService {

    @Resource
    private ChoiceJudgeSubmissionMapper choiceJudgeSubmissionMapper;

    @Resource
    private ChoiceProblemMapper choiceProblemMapper;

    @Resource
    private JudgeProblemMapper judgeProblemMapper;

    @Override
    public String submit(SubmissionRequest submissionRequest) {
        if (submissionRequest == null || submissionRequest.getProblemId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        ChoiceJudgeSubmission submission = new ChoiceJudgeSubmission();
        submission.setProblemId(submissionRequest.getProblemId());
        submission.setUserId(1L); // TODO: 从登录用户中获取
        submission.setAnswer(submissionRequest.getAnswer());
        submission.setSubmitTime(LocalDateTime.now());

        // 判断答案是否正确
        boolean isCorrect = false;
        if ("CHOICE".equals(submissionRequest.getType())) {
            ChoiceProblem problem = choiceProblemMapper.selectById(submissionRequest.getProblemId());
            isCorrect = problem != null && problem.getAnswer().equals(submissionRequest.getAnswer());
        } else if ("JUDGE".equals(submissionRequest.getType())) {
            JudgeProblem problem = judgeProblemMapper.selectById(submissionRequest.getProblemId());
            isCorrect = problem != null && 
                    problem.getAnswer().toString().equalsIgnoreCase(submissionRequest.getAnswer());
        }

        submission.setIsCorrect(isCorrect);

        // 保存提交记录
        boolean success = this.save(submission);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提交失败");
        }

        return isCorrect ? "回答正确" : "回答错误";
    }

    @Override
    public List<ChoiceJudgeSubmission> getUserProblemSubmissions(Long userId, Long problemId) {
        return choiceJudgeSubmissionMapper.getUserProblemSubmissions(userId, problemId);
    }

    @Override
    public List<ChoiceJudgeSubmission> getUserSubmissions(Long userId) {
        return choiceJudgeSubmissionMapper.getUserSubmissions(userId);
    }

    @Override
    public String getProblemAcceptRate(Long problemId) {
        return choiceJudgeSubmissionMapper.getProblemAcceptRate(problemId);
    }
} 