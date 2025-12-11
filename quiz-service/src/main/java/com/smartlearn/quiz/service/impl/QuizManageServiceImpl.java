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
import com.smartlearn.quiz.vo.QuizCreateResponseVO;
import com.smartlearn.quiz.vo.QuizDetailVO;
import com.smartlearn.quiz.vo.QuizListItemVO;
import com.smartlearn.quiz.vo.QuizQuestionItemVO;
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
    public QuizCreateResponseVO createQuiz(QuizCreateDTO dto, String teacherId) {

        // 1) 校验题目存在
        if (dto.getQuestionIds() == null || dto.getQuestionIds().isEmpty()) {
            throw new RuntimeException("题目列表不能为空");
        }
        // 批量查询 question 是否存在，并获取不存在的题目信息
        List<Question> existingQuestions = questionMapper.selectBatchIds(dto.getQuestionIds());
        List<String> existingQuestionIds = existingQuestions.stream()
                .map(Question::getId)
                .toList();

        // 找出所有不存在的题目ID
        List<String> nonExistentIds = dto.getQuestionIds().stream()
                .filter(id -> !existingQuestionIds.contains(id))
                .toList();

        if (!nonExistentIds.isEmpty()) {
            String errorMsg = "以下题目不存在: " + String.join(", ", nonExistentIds);
            throw new RuntimeException(errorMsg);
        }
        // 2) 插入 quiz
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setCourseId(dto.getCourseId());
        quiz.setChapterId(dto.getChapterId());
        quiz.setTotalScore(dto.getQuestionIds().size()); // 默认每题1分
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

        // 4) 构建返回对象
        return new QuizCreateResponseVO(
                quiz.getId(),
                quiz.getCourseId(),
                quiz.getChapterId(),
                quiz.getTitle(),
                quiz.getTotalScore(),
                quiz.getCreatedBy(),
                quiz.getCreatedTime() != null ? quiz.getCreatedTime().toString() : null,
                quiz.getUpdatedTime() != null ? quiz.getUpdatedTime().toString() : null
        );
    }

    @Override
    @Transactional
    public boolean deleteQuiz(String quizId) {
        if (quizId == null) {
            throw new RuntimeException("测验ID不能为空");
        }

        // 1) 检查测验是否存在
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new RuntimeException("测验不存在");
        }

        // 2) 删除测验题目关联记录
        quizQuestionMapper.delete(
                Wrappers.lambdaQuery(QuizQuestion.class)
                        .eq(QuizQuestion::getQuizId, quizId)
        );

        // 3) 删除测验主记录
        int result = quizMapper.deleteById(quizId);
        return result > 0;
    }

    @Override
    public List<QuizListItemVO> listByChapter(String chapterId) {
        List<Quiz> list = quizMapper.selectList(
                Wrappers.lambdaQuery(Quiz.class)
                        .eq(Quiz::getChapterId, chapterId)
                        .orderByAsc(Quiz::getCreatedTime)
        );

        return list.stream()
                .map(q -> new QuizListItemVO(q.getId(), q.getTitle(), q.getTotalScore(),
                    q.getCreatedTime() != null ? q.getCreatedTime().toString() : null))
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

        // 4) 组装题目简要信息
        List<QuizQuestionItemVO> questionItems = questions.stream()
                .map(q -> new QuizQuestionItemVO(q.getId(), q.getContent(), q.getKnowledgePoint()))
                .collect(Collectors.toList());

        // 5) 组装返回
        return new QuizDetailVO(
                quiz.getId(),
                quiz.getCourseId(),
                quiz.getChapterId(),
                quiz.getTitle(),
                quiz.getTotalScore(),
                questionItems
        );
    }
}
