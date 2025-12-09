package com.smartlearn.course.vo;

import com.smartlearn.course.entity.Course;
import com.smartlearn.course.entity.CourseChapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailVO {

    private Course course;

    private List<CourseChapter> chapters;
}
