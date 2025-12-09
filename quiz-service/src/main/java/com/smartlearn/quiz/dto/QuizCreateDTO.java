package com.smartlearn.quiz.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizCreateDTO {
    private String courseId;
    private String chapterId;
    private String title;
    private int totalScore;
    private List<String> questionIds;
}
