package com.smartlearn.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_course")
public class UserCourse {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String userId;

    private String courseId;

    /**
     * ENROLLED / DROPPED / COMPLETED
     */
    private String status;

    private LocalDateTime enrolledTime;

    private LocalDateTime completedTime;

}
