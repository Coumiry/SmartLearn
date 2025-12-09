package com.smartlearn.quiz.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlearn.common.annotation.TeacherOnly;
import com.smartlearn.common.response.R;
import com.smartlearn.quiz.dto.QuestionCreateRequest;
import com.smartlearn.quiz.dto.QuestionUpdateRequest;
import com.smartlearn.quiz.service.QuestionService;
import com.smartlearn.quiz.vo.QuestionDetailVO;
import com.smartlearn.quiz.vo.QuestionListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/quiz/manage/question")
@RequiredArgsConstructor
public class QuestionManageController {
    private final QuestionService questionService;
    @PostMapping("/create")
    @TeacherOnly
    public R<QuestionDetailVO> createQuestion(@RequestBody QuestionCreateRequest request, @RequestHeader("X-User-Id") String userId) {
        QuestionDetailVO questionDetailVO = questionService.createQuestion(request,userId);
        return R.ok(questionDetailVO);
    }
    @PutMapping("/update/{id}")
    @TeacherOnly
    public R<QuestionDetailVO> updateQuestion(@RequestBody QuestionUpdateRequest request, @PathVariable String id, @RequestHeader("X-User-Id") String userId,@RequestHeader("X-User-Role") String userRole) {
        QuestionDetailVO questionDetailVO = questionService.updateQuestion(id,request,userId,userRole);
        return R.ok(questionDetailVO);
    }
    @DeleteMapping("/delete/{id}")
    @TeacherOnly
    public R<Boolean> deleteQuestion(@PathVariable String id,@RequestHeader("X-User-Id") String userId,@RequestHeader("X-User-Role") String userRole) {
        questionService.deleteQuestion(id,userId,userRole);
        return R.ok(true);
    }
    @GetMapping("/list")
    @TeacherOnly
    public R<Page<QuestionListItemVO>> list(@RequestParam(defaultValue = "1") Integer pageNo,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String questionType,
                                            @RequestParam(required = false) String knowledgePoint,
                                            @RequestParam(required = false) String keyword) {
        Page<QuestionListItemVO> page = questionService.pageQuery(pageNo, pageSize,
                questionType, knowledgePoint, keyword);
        return R.ok(page);
    }
}
