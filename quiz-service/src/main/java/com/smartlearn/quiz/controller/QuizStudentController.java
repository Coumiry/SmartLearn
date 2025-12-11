package com.smartlearn.quiz.controller;

import com.smartlearn.common.response.R;
import com.smartlearn.quiz.service.QuizStudentService;
import com.smartlearn.quiz.vo.QuizStudentDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学生测验接口
 * 学生端测验相关接口
 */
@RestController
@RequestMapping("/quiz/student")
@RequiredArgsConstructor
public class QuizStudentController {

    private final QuizStudentService quizStudentService;

    /**
     * 获取试卷详情
     * 学生根据测验ID获取题目列表，不暴露标准答案
     * @param quizId 测验ID
     * @return 试卷详情
     */
    @GetMapping("/detail/{quizId}")
    public R<QuizStudentDetailVO> getQuizDetail(@PathVariable String quizId) {
        QuizStudentDetailVO quizDetail = quizStudentService.getQuizDetailForStudent(quizId);
        return R.ok(quizDetail);
    }
}