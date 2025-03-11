package com.oj.model.request;

import com.oj.model.dto.ProblemOptionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceProblemAddRequest extends ProblemAddRequest {
    @NotNull(message = "选项不能为空")
    private List<ProblemOptionDTO> options;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String analysis;
} 