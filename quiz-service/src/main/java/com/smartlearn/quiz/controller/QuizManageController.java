package com.smartlearn.quiz.controller;

import com.smartlearn.common.response.R;
import com.smartlearn.quiz.dto.QuizCreateDTO;
import com.smartlearn.quiz.service.QuizManageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/quiz/manage")
public class QuizManageController {

    @Resource
    private QuizManageService quizManageService;

    @PostMapping("/create")
    public R<?> createQuiz(@RequestBody QuizCreateDTO dto,
                           @RequestHeader("X-User-Id") String teacherId,
                           @RequestHeader("X-User-Role") String role) {

        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            return R.fail("无权限");
        }

        String quizId = quizManageService.createQuiz(dto, teacherId);
        return R.ok(Map.of("id", quizId));
    }

    @GetMapping("/{chapterId}/list")
    public R<?> listByChapter(@PathVariable String chapterId,
                              @RequestHeader("X-User-Role") String role) {

        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            return R.fail("无权限");
        }

        return R.ok(quizManageService.listByChapter(chapterId));
    }

    @GetMapping("/detail/{quizId}")
    public R<?> detail(@PathVariable String quizId,
                       @RequestHeader("X-User-Role") String role) {

        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            return R.fail("无权限");
        }

        return R.ok(quizManageService.detail(quizId));
    }
}
