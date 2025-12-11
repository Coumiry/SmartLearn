package com.smartlearn.quiz.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class QuestionUpdateRequest {
    @NotBlank(message = "题干不能为空")
    private String content;

    private String questionType;

    private String correctAnswer;

    private String analysis;

    private String knowledgePoint;

}
