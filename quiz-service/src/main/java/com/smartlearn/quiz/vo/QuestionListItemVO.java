package com.smartlearn.quiz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionListItemVO {
    private String id;
    private String questionType;
    private String content;
    private String createdBy;
    private LocalDateTime createdTime;
    private String knowledgePoint;
}
