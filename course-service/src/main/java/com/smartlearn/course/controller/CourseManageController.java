package com.smartlearn.course.controller;

import com.smartlearn.common.response.R;
import com.smartlearn.course.annotation.TeacherOnly;
import com.smartlearn.course.entity.Course;
import com.smartlearn.course.entity.CourseChapter;
import com.smartlearn.course.exception.ForbiddenException;
import com.smartlearn.course.service.CourseChapterService;
import com.smartlearn.course.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course/manage")
@RequiredArgsConstructor
public class CourseManageController {

    private final CourseService courseService;
    private final CourseChapterService courseChapterService;

    /**
     * 从请求头获取当前用户ID
     * 网关会在通过 JWT 校验后把 userId 塞到 X-User-Id
     */
    private String getCurrentUserId(HttpServletRequest request) {
        String id = request.getHeader("X-User-Id");
        return id == null ? null : id;
    }
    /**
     * 校验课程存在且属于当前教师；返回课程实体
     */
    private Course assertCourseOwnedByCurrentTeacher(String courseId, String teacherId) {
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        if (!teacherId.equals(course.getTeacherId())) {
            throw new ForbiddenException("只能操作自己创建的课程");
        }
        return course;
    }

    /**
     * 创建课程（仅教师/管理员）
     * 目前DRAFT状态也是默认的，回头再改
     */
    @PostMapping("/create")
    @TeacherOnly
    public Course createCourse(@RequestBody Course course, HttpServletRequest request) {
        String teacherId = getCurrentUserId(request);
        course.setTeacherId(teacherId);
        course.setStatus("DRAFT"); // 初始为草稿

        courseService.save(course);
        return course;
    }

    /**
     * 更新课程（仅教师/管理员）
     */
    @PutMapping("/update/{courseId}")
    @TeacherOnly
    public Boolean updateCourse(@PathVariable String courseId,
                                @RequestBody Course course,
                                HttpServletRequest request) {
        String currentTeacherId = getCurrentUserId(request);

        // 1. 先查出数据库里的原始课程信息
        Course dbCourse = courseService.getById(courseId);
        if (dbCourse == null) {
            throw new RuntimeException("课程不存在");
        }

        // 2. 校验当前登录老师是不是这门课的拥有者
        if (!currentTeacherId.equals(dbCourse.getTeacherId())) {
            // 这里用你自己的 ForbiddenException 更好
            throw new ForbiddenException("只能修改自己创建的课程");
        }

        // 3. 合并要更新的字段（简单做法：用前端传来的覆盖）
        course.setId(courseId);
        course.setTeacherId(dbCourse.getTeacherId()); // 防止前端乱改 teacherId

        return courseService.updateById(course);
    }

    /**
     * 发布课程（仅教师/管理员）
     */
    @PutMapping("/publish/{courseId}")
    @TeacherOnly
    public Boolean publishCourse(@PathVariable String courseId) {
        return courseService.lambdaUpdate()
                .eq(Course::getId, courseId)
                .set(Course::getStatus, "PUBLISHED")
                .update();
    }

    /**
     * 查询当前教师自己的课程列表
     */
    @GetMapping("/my")
    @TeacherOnly
    public List<Course> myCourses(HttpServletRequest request) {
        String teacherId = getCurrentUserId(request);

        return courseService.lambdaQuery()
                .eq(Course::getTeacherId, teacherId)
                .list();
    }

    /* ---------- 下面是你要的 Chapter CRUD 业务代码 ---------- */

    /**
     * 创建章节
     * POST /course/manage/{courseId}/chapter/create
     */
    @PostMapping("/{courseId}/chapter/create")
    @TeacherOnly
    public CourseChapter createChapter(@PathVariable String courseId,
                                       @RequestBody CourseChapter chapter,
                                       HttpServletRequest request) {
        String teacherId = getCurrentUserId(request);
        // 1. 校验课程是否属于当前老师
        assertCourseOwnedByCurrentTeacher(courseId, teacherId);

        // 2. 设置课程ID，防止前端乱传
        chapter.setId(null); // 让 MyBatis-Plus 生成ID
        chapter.setCourseId(courseId);

        // 3. 保存
        courseChapterService.save(chapter);
        return chapter;
    }

    /**
     * 更新章节
     * PUT /course/manage/chapter/update/{chapterId}
     */
    @PutMapping("/chapter/update/{chapterId}")
    @TeacherOnly
    public Boolean updateChapter(@PathVariable String chapterId,
                                 @RequestBody CourseChapter chapter,
                                 HttpServletRequest request) {
        String teacherId = getCurrentUserId(request);

        // 1. 查出章节
        CourseChapter dbChapter = courseChapterService.getById(chapterId);
        if (dbChapter == null) {
            throw new RuntimeException("章节不存在");
        }

        // 2. 校验该章节对应的课程是否属于当前教师
        assertCourseOwnedByCurrentTeacher(dbChapter.getCourseId(), teacherId);

        // 3. 只更新允许修改的字段（标题、排序、资源地址），防止误改 courseId
        dbChapter.setTitle(chapter.getTitle());
        dbChapter.setSortOrder(chapter.getSortOrder());
        dbChapter.setVideoUrl(chapter.getVideoUrl());
        dbChapter.setDocUrl(chapter.getDocUrl());

        return courseChapterService.updateById(dbChapter);
    }

    /**
     * 删除章节
     * DELETE /course/manage/chapter/delete/{chapterId}
     */
    @DeleteMapping("/chapter/delete/{chapterId}")
    @TeacherOnly
    public Boolean deleteChapter(@PathVariable String chapterId,
                                 HttpServletRequest request) {
        String teacherId = getCurrentUserId(request);

        // 1. 查出章节
        CourseChapter dbChapter = courseChapterService.getById(chapterId);
        if (dbChapter == null) {
            // 没有也算“删除成功”，看你业务习惯，这里直接 false 也可以
            return false;
        }

        // 2. 校验所属课程是否归当前老师
        assertCourseOwnedByCurrentTeacher(dbChapter.getCourseId(), teacherId);

        // 3. 删除
        return courseChapterService.removeById(chapterId);
    }

    /**
     * 查询某门课程的章节列表（教师视角）
     * GET /course/manage/{courseId}/chapter/list
     */
    @GetMapping("/{courseId}/chapter/list")
    @TeacherOnly
    public List<CourseChapter> listChapters(@PathVariable String courseId,
                                            HttpServletRequest request) {
        String teacherId = getCurrentUserId(request);

        // 1. 校验课程归属
        assertCourseOwnedByCurrentTeacher(courseId, teacherId);

        // 2. 查询该课程下所有章节，按 sort_order 升序
        return courseChapterService.lambdaQuery()
                .eq(CourseChapter::getCourseId, courseId)
                .orderByAsc(CourseChapter::getSortOrder)
                .list();
    }
}
