package com.smartlearn.quiz.service;

import com.smartlearn.quiz.dto.QuizCreateDTO;
import com.smartlearn.quiz.vo.QuizCreateResponseVO;
import com.smartlearn.quiz.vo.QuizDetailVO;
import com.smartlearn.quiz.vo.QuizListItemVO;

import java.util.List;

public interface QuizManageService {
    QuizCreateResponseVO createQuiz(QuizCreateDTO dto, String teacherId);
    List<QuizListItemVO> listByChapter(String chapterId);

    QuizDetailVO detail(String quizId);
    boolean deleteQuiz(String quizId);
}
