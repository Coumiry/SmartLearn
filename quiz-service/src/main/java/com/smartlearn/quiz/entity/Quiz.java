package com.smartlearn.quiz.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("quiz")
public class Quiz {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String courseId;
    private String chapterId;
    private String title;
    private Integer totalScore;
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
