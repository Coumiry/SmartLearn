package com.smartlearn.course.controller;

import com.smartlearn.common.response.R;
import com.smartlearn.course.entity.Course;
import com.smartlearn.course.entity.CourseChapter;
import com.smartlearn.course.vo.CourseDetailVO;
import com.smartlearn.course.service.CourseChapterService;
import com.smartlearn.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course/public")
@RequiredArgsConstructor
public class CoursePublicController {
    private final CourseService courseService;
    private final CourseChapterService chapterService;

    /**
     * 分页查询，但是还没分，要用Page<T>
     * 再就是"PUBLISHED"状态目前是写死的（在CourseManageController下的publish)
     * @return
     */
    @PostMapping("/list")
    public List<Course> list(){
        return courseService.lambdaQuery()
                .eq(Course::getStatus, "PUBLISHED")
                .list();
    }

    /**
     * 课程详情
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public R<CourseDetailVO> detail(@PathVariable String id) {
        Course course = courseService.getById(id);
        if (course == null) {
            return R.fail("课程不存在");
        }

        List<CourseChapter> chapters = chapterService
                .lambdaQuery()
                .eq(CourseChapter::getCourseId, id)
                .orderByAsc(CourseChapter::getSortOrder)
                .list();

        return R.ok(new CourseDetailVO(course,chapters));
    }

}
