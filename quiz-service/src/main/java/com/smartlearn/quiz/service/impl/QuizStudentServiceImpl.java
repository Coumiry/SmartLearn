package com.smartlearn.quiz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartlearn.quiz.entity.Quiz;
import com.smartlearn.quiz.entity.QuizQuestion;
import com.smartlearn.quiz.entity.Question;
import com.smartlearn.quiz.exception.QuizException;
import com.smartlearn.quiz.mapper.QuizMapper;
import com.smartlearn.quiz.mapper.QuizQuestionMapper;
import com.smartlearn.quiz.mapper.QuestionMapper;
import com.smartlearn.quiz.service.QuizStudentService;
import com.smartlearn.quiz.vo.QuizStudentDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizStudentServiceImpl implements QuizStudentService {

    private final QuizMapper quizMapper;
    private final QuizQuestionMapper quizQuestionMapper;
    private final QuestionMapper questionMapper;

    @Override
    public QuizStudentDetailVO getQuizDetailForStudent(String quizId) {
        // 1. 查询测验基本信息
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new QuizException("测验不存在");
        }

        // 2. 查询测验题目关联关系
        List<QuizQuestion> quizQuestions = quizQuestionMapper.selectList(
                new LambdaQueryWrapper<QuizQuestion>()
                        .eq(QuizQuestion::getQuizId, quizId)
        );

        if (quizQuestions.isEmpty()) {
            throw new QuizException("测验中没有题目");
        }

        // 3. 查询题目详情（不包含答案）
        List<String> questionIds = quizQuestions.stream()
                .map(QuizQuestion::getQuestionId)
                .collect(Collectors.toList());

        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .in(Question::getId, questionIds)
        );

        // 4. 构建返回数据
        List<QuizStudentDetailVO.QuestionForQuizVO> questionVOs = questions.stream()
                .map(question -> QuizStudentDetailVO.QuestionForQuizVO.builder()
                        .questionId(question.getId())
                        .content(question.getContent())
                        .score(1) // 暂时每题1分
                        .knowledgePoint(question.getKnowledgePoint())
                        .build())
                .collect(Collectors.toList());

        return QuizStudentDetailVO.builder()
                .quizId(quiz.getId())
                .title(quiz.getTitle())
                .totalScore(quiz.getTotalScore())
                .questions(questionVOs)
                .build();
    }
}