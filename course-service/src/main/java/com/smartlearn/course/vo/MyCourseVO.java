package com.smartlearn.course.vo;

import lombok.Data;

@Data
public class MyCourseVO {

    private String courseId;

    private String title;

    private String description;

    private String coverUrl;

    private String status; // 选课状态
}
