package com.smartlearn.quiz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizCreateResponseVO {
    private String id;
    private String courseId;
    private String chapterId;
    private String title;
    private Integer totalScore;
    private String createdBy;
    private String createdTime;
    private String updatedTime;
}