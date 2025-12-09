package com.smartlearn.quiz.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("question_option")
public class QuestionOption {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String questionId;
    private String optionKey;
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
