package com.smartlearn.quiz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.smartlearn.quiz.dto.QuizCreateDTO;
import com.smartlearn.quiz.entity.Question;
import com.smartlearn.quiz.entity.Quiz;
import com.smartlearn.quiz.entity.QuizQuestion;
import com.smartlearn.quiz.mapper.QuestionMapper;
import com.smartlearn.quiz.mapper.QuizMapper;
import com.smartlearn.quiz.mapper.QuizQuestionMapper;
import com.smartlearn.quiz.service.QuizManageService;
import com.smartlearn.quiz.vo.QuizDetailVO;
import com.smartlearn.quiz.vo.QuizListItemVO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizManageServiceImpl implements QuizManageService {

    @Resource
    private QuizMapper quizMapper;
    @Resource
    private QuizQuestionMapper quizQuestionMapper;
    @Resource
    private QuestionMapper questionMapper; // 校验题目存在

    @Override
    @Transactional
    public String createQuiz(QuizCreateDTO dto, String teacherId) {

        // 1) 校验题目存在
        if (dto.getQuestionIds() == null || dto.getQuestionIds().isEmpty()) {
            throw new RuntimeException("题目列表不能为空");
        }
        // 可选：批量查询 question 是否存在

        // 2) 插入 quiz
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setCourseId(dto.getCourseId());
        quiz.setChapterId(dto.getChapterId());
        quiz.setCreatedBy(teacherId);
        quiz.setCreatedTime(LocalDateTime.now());

        quizMapper.insert(quiz);

        // 3) 插入 quiz_question
        for (String qid : dto.getQuestionIds()) {
            QuizQuestion record = new QuizQuestion();
            record.setQuizId(quiz.getId());
            record.setQuestionId(qid);
            quizQuestionMapper.insert(record);
        }

        return quiz.getId();
    }
    @Override
    public List<QuizListItemVO> listByChapter(String chapterId) {
        List<Quiz> list = quizMapper.selectList(
                Wrappers.lambdaQuery(Quiz.class)
                        .eq(Quiz::getChapterId, chapterId)
                        .orderByAsc(Quiz::getCreatedTime)
        );

        return list.stream()
                .map(q -> new QuizListItemVO(q.getId(), q.getTitle()))
                .collect(Collectors.toList());
    }
    @Override
    public QuizDetailVO detail(String quizId) {

        // 1) 查询 quiz
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new RuntimeException("测验不存在");
        }

        // 2) 查询测验题目 ID 列表
        List<String> qids = quizQuestionMapper.selectList(
                        Wrappers.lambdaQuery(QuizQuestion.class)
                                .eq(QuizQuestion::getQuizId, quizId)
                ).stream()
                .map(QuizQuestion::getQuestionId)
                .toList();

        // 3) 查询题目内容（批量）
        List<Question> questions = questionMapper.selectBatchIds(qids);

        // 4) 组装返回
        QuizDetailVO vo = new QuizDetailVO();
        vo.setQuizId(quizId);
        vo.setTitle(quiz.getTitle());
        vo.setQuestions(questions);

        return vo;
    }
}
