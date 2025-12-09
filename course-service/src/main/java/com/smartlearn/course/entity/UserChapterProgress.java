package com.smartlearn.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_chapter_progress")
public class UserChapterProgress {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("chapter_id")   // ★ 显式指定数据库列名
    private String chapterId;

    /**
     * NOT_STARTED / LEARNING / FINISHED
     */
    private String status;

    @TableField(value = "last_learn_time",fill =  FieldFill.INSERT_UPDATE)
    private LocalDateTime lastLearnTime;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
