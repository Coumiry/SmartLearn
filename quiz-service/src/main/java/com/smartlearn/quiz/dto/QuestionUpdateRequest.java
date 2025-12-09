package com.smartlearn.quiz.dto;

import lombok.Data;

@Data
public class QuestionUpdateRequest {
    private String questionType;
    private String content;
    private String correctAnswer;
    private String analysis;
    private String knowledgePoint;
}
