package com.smartlearn.quiz.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class QuestionCreateRequest {
    @NotBlank(message = "题干不能为空")
    private String content;

    @NotNull(message = "题型不能为空")
    private String questionType;

    @NotBlank(message = "正确答案不能为空")
    private String correctAnswer;

    private String analysis;

    private String knowledgePoint;
}
