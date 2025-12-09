package com.smartlearn.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlearn.course.entity.CourseChapter;
import com.smartlearn.course.mapper.CourseChapterMapper;
import com.smartlearn.course.service.CourseChapterService;
import org.springframework.stereotype.Service;

@Service
public class CourseChapterServiceImpl
        extends ServiceImpl<CourseChapterMapper, CourseChapter>
        implements CourseChapterService {
}
