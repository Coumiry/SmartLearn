package com.smartlearn.quiz.vo;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class QuizStudentDetailVO {
    private String quizId;
    private String title;
    private Integer totalScore;
    private List<QuestionForQuizVO> questions;

    @Data
    @Builder
    public static class QuestionForQuizVO {
        private String questionId;
        private String content;
        private Integer score;
        private String knowledgePoint;
    }
}