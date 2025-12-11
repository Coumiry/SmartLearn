package com.smartlearn.quiz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizQuestionItemVO {
    private String questionId;
    private String content;
    private String knowledgePoint;
}
