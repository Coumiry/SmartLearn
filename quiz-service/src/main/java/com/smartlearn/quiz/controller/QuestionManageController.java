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
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
@Slf4j
@RestController
@RequestMapping("/quiz/manage/question")
@RequiredArgsConstructor
@Validated
public class QuestionManageController {
    private final QuestionService questionService;

    @PostMapping("/create")
    @TeacherOnly
    public R<QuestionDetailVO> createQuestion(@Valid @RequestBody QuestionCreateRequest request,
                                             @RequestHeader("X-User-Id") String userId) {
        log.info("Teacher {} creating question: {}", userId, request.getContent().substring(0, Math.min(50, request.getContent().length())));
        QuestionDetailVO questionDetailVO = questionService.createQuestion(request, userId);
        return R.ok(questionDetailVO);
    }

    @PutMapping("/update/{id}")
    @TeacherOnly
    public R<QuestionDetailVO> updateQuestion(@Valid @RequestBody QuestionUpdateRequest request,
                                             @PathVariable String id,
                                             @RequestHeader("X-User-Id") String userId,
                                             @RequestHeader("X-User-Role") String userRole) {
        log.info("Teacher {} updating question: {}", userId, id);
        QuestionDetailVO questionDetailVO = questionService.updateQuestion(id, request, userId, userRole);
        return R.ok(questionDetailVO);
    }

    @DeleteMapping("/delete/{id}")
    @TeacherOnly
    public R<Boolean> deleteQuestion(@PathVariable String id,
                                    @RequestHeader("X-User-Id") String userId,
                                    @RequestHeader("X-User-Role") String userRole) {
        log.info("Teacher {} deleting question: {}", userId, id);
        questionService.deleteQuestion(id, userId, userRole);
        return R.ok(true);
    }

    @GetMapping("/list")
    @TeacherOnly
    public R<Page<QuestionListItemVO>> list(
            @Min(value = 1, message = "页码必须大于0") @RequestParam(defaultValue = "1") Integer pageNo,
            @Min(value = 1, message = "每页大小必须大于0")
            @Max(value = 100, message = "每页大小不能超过100") @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) String knowledgePoint,
            @RequestParam(required = false) String keyword) {

        log.info("Querying questions: pageNo={}, pageSize={}, questionType={}, knowledgePoint={}, keyword={}",
                pageNo, pageSize, questionType, knowledgePoint, keyword);

        Page<QuestionListItemVO> page = questionService.pageQuery(pageNo, pageSize,
                questionType, knowledgePoint, keyword);
        return R.ok(page);
    }
}
