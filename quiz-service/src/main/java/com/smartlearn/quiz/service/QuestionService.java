package com.smartlearn.quiz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartlearn.quiz.dto.QuestionCreateRequest;
import com.smartlearn.quiz.dto.QuestionUpdateRequest;
import com.smartlearn.quiz.entity.Question;
import com.smartlearn.quiz.vo.QuestionDetailVO;
import com.smartlearn.quiz.vo.QuestionListItemVO;

public interface QuestionService extends IService<Question> {
    QuestionDetailVO createQuestion(QuestionCreateRequest request, String userId);
    QuestionDetailVO updateQuestion(String id,QuestionUpdateRequest request, String userId,String userRole);
    void deleteQuestion(String id, String teacherId, String userRole);

    Page<QuestionListItemVO> pageQuery(Integer pageNo, Integer pageSize,
                                       String questionType,
                                       String knowledgePoint,
                                       String keyword);
}
