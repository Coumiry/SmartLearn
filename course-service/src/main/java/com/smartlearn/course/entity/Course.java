package com.smartlearn.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String title;

    private String description;

    /**
     * BEGINNER / INTERMEDIATE / ADVANCED
     */
    private String level;

    private String coverUrl;

    private String teacherId;

    /**
     * DRAFT / PUBLISHED
     */
    private String status;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
