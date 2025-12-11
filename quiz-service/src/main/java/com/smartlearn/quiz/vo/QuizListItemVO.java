package com.smartlearn.quiz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizListItemVO {
    private String id;
    private String title;
    private Integer totalScore;
    private String createdTime;
}
