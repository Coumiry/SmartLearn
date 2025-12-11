package com.smartlearn.quiz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizDetailVO {
    private String id;
    private String courseId;
    private String chapterId;
    private String title;
    private Integer totalScore;
    private List<QuizQuestionItemVO> questions;
}

