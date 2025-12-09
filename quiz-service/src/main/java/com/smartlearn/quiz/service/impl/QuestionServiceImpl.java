package com.smartlearn.quiz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlearn.common.response.R;
import com.smartlearn.quiz.dto.QuestionCreateRequest;
import com.smartlearn.quiz.dto.QuestionUpdateRequest;
import com.smartlearn.quiz.entity.Question;
import com.smartlearn.quiz.entity.QuizQuestion;
import com.smartlearn.quiz.mapper.QuestionMapper;
import com.smartlearn.quiz.mapper.QuizQuestionMapper;
import com.smartlearn.quiz.service.QuestionService;
import com.smartlearn.quiz.vo.QuestionDetailVO;
import com.smartlearn.quiz.vo.QuestionListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    private final QuizQuestionMapper quizQuestionMapper;
    private QuestionDetailVO buildDetailVO(Question question) {
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        BeanUtils.copyProperties(question, questionDetailVO);
        return questionDetailVO;
    }
    @Override
    @Transactional
    public QuestionDetailVO createQuestion(QuestionCreateRequest request,String userId) {
        Question question = new Question();
        String type = "SHORT";
        question.setQuestionType(type);
        question.setContent(request.getContent());
        question.setCreatedBy(userId);
        question.setAnalysis(request.getAnalysis());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setKnowledgePoint(request.getKnowledgePoint());
        this.save(question);
        return buildDetailVO(question);
    }
    @Override
    @Transactional
    public QuestionDetailVO updateQuestion(String id,QuestionUpdateRequest request, String userId,String userRole) {
        Question current_question = this.getById(id);
        if(current_question == null){
            throw new RuntimeException("题目不存在");
        }
        if(!userId.equals(current_question.getCreatedBy()) && !"ADMIN".equalsIgnoreCase(userRole)){
            throw new RuntimeException("无权修改该题目");
        }
        if (request.getQuestionType() != null && !request.getQuestionType().isEmpty()) {
            current_question.setQuestionType(request.getQuestionType());
        }
        current_question.setContent(request.getContent());
        current_question.setCorrectAnswer(request.getCorrectAnswer());
        current_question.setAnalysis(request.getAnalysis());
        current_question.setKnowledgePoint(request.getKnowledgePoint());
        this.updateById(current_question);
        return buildDetailVO(current_question);
    }
    @Override
    @Transactional
    public void deleteQuestion(String id, String teacherId, String userRole){
        Question current_question = this.getById(id);
        if(current_question == null){
            throw new RuntimeException("题目不存在");
        }
        if(!userRole.equals(current_question.getCreatedBy())||!"ADMIN".equalsIgnoreCase(userRole)){
            throw new RuntimeException("无权修改该题目");
        }
        LambdaQueryWrapper<QuizQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizQuestion::getQuestionId, id);
        quizQuestionMapper.delete(wrapper);
        this.removeById(id);
    }
    @Override
    public Page<QuestionListItemVO> pageQuery(Integer pageNo, Integer pageSize, String questionType, String knowledgePoint, String keyword){
        Page<Question> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();

        if (questionType != null && !questionType.isEmpty()) {
            wrapper.eq(Question::getQuestionType, questionType);
        }
        if (knowledgePoint != null && !knowledgePoint.isEmpty()) {
            wrapper.like(Question::getKnowledgePoint, knowledgePoint);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Question::getContent, keyword);
        }

        wrapper.orderByDesc(Question::getCreatedTime);

        this.page(page, wrapper);

        Page<QuestionListItemVO> voPage = new Page<>();
        voPage.setCurrent(page.getCurrent());
        voPage.setSize(page.getSize());
        voPage.setTotal(page.getTotal());

        List<QuestionListItemVO> records = page.getRecords().stream().map(q -> {
            QuestionListItemVO vo = new QuestionListItemVO();
            vo.setId(q.getId());
            vo.setQuestionType(q.getQuestionType());
            vo.setContent(q.getContent());
            vo.setKnowledgePoint(q.getKnowledgePoint());
            vo.setCreatedBy(q.getCreatedBy());
            vo.setCreatedTime(q.getCreatedTime());
            return vo;
        }).toList();

        voPage.setRecords(records);
        return voPage;
    }
}
