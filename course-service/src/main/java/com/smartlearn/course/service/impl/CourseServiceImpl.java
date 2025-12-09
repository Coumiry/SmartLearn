package com.smartlearn.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlearn.course.entity.Course;
import com.smartlearn.course.mapper.CourseMapper;
import com.smartlearn.course.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
}
