package com.smartlearn.course.controller;

import com.smartlearn.common.response.R;
import com.smartlearn.course.vo.ChapterProgressVO;
import com.smartlearn.course.vo.MyCourseVO;
import com.smartlearn.course.service.ChapterProgressService;
import com.smartlearn.course.service.EnrollmentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseLearningController {

    private final EnrollmentService enrollmentService;
    private final ChapterProgressService chapterProgressService;

    /**
     * 从网关透传的请求头获取当前用户ID
     */
    private String getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            // 按你项目的风格抛业务异常
            throw new RuntimeException("未登录或缺少用户信息");
        }
        return userIdStr;
    }

    @PostMapping("/enroll/{courseId}")
    public R<Void> enroll(@PathVariable String courseId, HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        enrollmentService.enroll(userId, courseId);
        return R.ok();
    }

    @GetMapping("/my")
    public R<List<MyCourseVO>> myCourses(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        List<MyCourseVO> list = enrollmentService.listMyCourses(userId);
        return R.ok(list);
    }

    // 可选：退课
    @PostMapping("/enroll/{courseId}/drop")
    public R<Void> drop(@PathVariable String courseId, HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        enrollmentService.dropCourse(userId, courseId);
        return R.ok();
    }

    // ========== 学习进度 ==========

    @PostMapping("/progress/update")
    public R<Void> updateProgress(@RequestBody UpdateProgressRequest req,
                                             HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        chapterProgressService.updateProgress(userId, req.getChapterId(), req.getStatus());
        return R.ok();
    }

    @GetMapping("/progress/{courseId}")
    public R<List<ChapterProgressVO>> getCourseProgress(@PathVariable String courseId,
                                                                   HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        List<ChapterProgressVO> list = chapterProgressService.getCourseProgress(userId, courseId);
        return R.ok(list);
    }

    @Data
    public static class UpdateProgressRequest {
        private String chapterId;
        /**
         * NOT_STARTED / LEARNING / FINISHED
         * 前端第一版可以只传 FINISHED
         */
        private String status;
    }
}
