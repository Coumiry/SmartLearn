package com.smartlearn.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_chapter")
public class CourseChapter {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String courseId;

    private String title;

    private Integer sortOrder;

    private String videoUrl;

    private String docUrl;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
