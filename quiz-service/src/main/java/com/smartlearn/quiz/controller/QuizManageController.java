package com.smartlearn.quiz.controller;

import com.smartlearn.common.response.R;
import com.smartlearn.quiz.dto.QuizCreateDTO;
import com.smartlearn.quiz.service.QuizManageService;
import com.smartlearn.quiz.vo.QuizCreateResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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

        QuizCreateResponseVO response = quizManageService.createQuiz(dto, teacherId);
        return R.ok(response);
    }

    @GetMapping("/quiz/list")
    public R<?> listQuizzes(@RequestParam String courseId,
                            @RequestParam String chapterId,
                            @RequestHeader("X-User-Role") String role) {

        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            return R.fail("无权限");
        }

        return R.ok(quizManageService.listByChapter(chapterId));
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

    @DeleteMapping("/quiz/delete/{quizId}")
    public R<?> deleteQuiz(@PathVariable String quizId,
                           @RequestHeader("X-User-Role") String role) {

        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            return R.fail("无权限");
        }

        boolean deleted = quizManageService.deleteQuiz(quizId);
        return R.ok(deleted);
    }
}
