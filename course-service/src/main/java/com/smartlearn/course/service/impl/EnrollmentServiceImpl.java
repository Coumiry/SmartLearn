package com.smartlearn.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartlearn.course.entity.Course;
import com.smartlearn.course.entity.UserCourse;
import com.smartlearn.course.enums.EnrollStatusEnum;
import com.smartlearn.course.mapper.CourseMapper;
import com.smartlearn.course.mapper.UserCourseMapper;
import com.smartlearn.course.vo.MyCourseVO;
import com.smartlearn.course.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final UserCourseMapper userCourseMapper;
    private final CourseMapper courseMapper;

    @Override
    public void enroll(String userId, String courseId) {
        // 是否已有记录
        UserCourse exist = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getCourseId, courseId)
        );
        LocalDateTime now = LocalDateTime.now();
        if (exist == null) {
            UserCourse uc = new UserCourse();
            uc.setUserId(userId);
            uc.setCourseId(courseId);
            uc.setStatus(EnrollStatusEnum.ENROLLED.getCode());
            uc.setEnrolledTime(now);
            userCourseMapper.insert(uc);
        } else {
            // 已有记录，可能是 DROPPED，重新设为 ENROLLED
            exist.setStatus(EnrollStatusEnum.ENROLLED.getCode());
            exist.setEnrolledTime(now);
            userCourseMapper.updateById(exist);
        }
    }

    @Override
    public List<MyCourseVO> listMyCourses(String userId) {
        List<UserCourse> userCourses = userCourseMapper.selectList(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getStatus, EnrollStatusEnum.ENROLLED.getCode())
        );
        if (userCourses.isEmpty()) {
            return List.of();
        }
        List<String> courseIds = userCourses.stream()
                .map(UserCourse::getCourseId)
                .toList();

        List<Course> courses = courseMapper.selectBatchIds(courseIds);

        return courses.stream().map(course -> {
            MyCourseVO vo = new MyCourseVO();
            vo.setCourseId(course.getId());
            vo.setTitle(course.getTitle());
            vo.setDescription(course.getDescription());
            vo.setCoverUrl(course.getCoverUrl());
            // 找到对应的选课记录取状态
            String status = userCourses.stream()
                    .filter(uc -> uc.getCourseId().equals(course.getId()))
                    .findFirst()
                    .map(UserCourse::getStatus)
                    .orElse(EnrollStatusEnum.ENROLLED.getCode());
            vo.setStatus(status);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void dropCourse(String userId, String courseId) {
        UserCourse exist = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getCourseId, courseId)
        );
        if (exist != null) {
            exist.setStatus(EnrollStatusEnum.DROPPED.getCode());
            userCourseMapper.updateById(exist);
        }
    }
}
