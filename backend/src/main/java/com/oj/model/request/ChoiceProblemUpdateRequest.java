package com.oj.model.request;

import com.oj.model.dto.ProblemOptionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceProblemUpdateRequest extends ProblemUpdateRequest {
    /**
     * 选项列表
     */
    @NotEmpty(message = "选项不能为空")
    private List<ProblemOptionDTO> options;

    /**
     * 正确答案
     */
    @NotBlank(message = "答案不能为空")
    private String answer;

    /**
     * 解析
     */
    private String analysis;
} 