package com.smartlearn.course.service;
import com.smartlearn.course.vo.MyCourseVO;

import java.util.List;

public interface EnrollmentService {

    /**
     * 选课
     */
    void enroll(String userId, String courseId);

    /**
     * 我的课程列表
     */
    List<MyCourseVO> listMyCourses(String userId);

    /**
     * 退课（可选）
     */
    void dropCourse(String userId, String  courseId);
}
