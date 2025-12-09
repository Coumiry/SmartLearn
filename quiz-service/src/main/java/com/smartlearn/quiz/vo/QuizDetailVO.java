package com.smartlearn.quiz.vo;

import com.smartlearn.quiz.entity.Question;
import lombok.Data;

import java.util.List;

@Data
public class QuizDetailVO {
    private String quizId;
    private String title;
    private List<Question> questions;
}
