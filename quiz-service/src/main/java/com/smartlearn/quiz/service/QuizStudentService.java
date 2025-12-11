package com.smartlearn.quiz.service;

import com.smartlearn.quiz.vo.QuizStudentDetailVO;

public interface QuizStudentService {

    /**
     * 获取学生测验详情（不包含答案）
     * @param quizId 测验ID
     * @return 测验详情
     */
    QuizStudentDetailVO getQuizDetailForStudent(String quizId);
}