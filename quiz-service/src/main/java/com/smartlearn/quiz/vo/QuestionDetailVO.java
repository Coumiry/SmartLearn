package com.smartlearn.quiz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionDetailVO {
    private String id;
    private String questionType;
    private String content;
    private String correctAnswer;
    private String analysis;
    private String knowledgePoint;
    private String createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
