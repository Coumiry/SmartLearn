package com.smartlearn.quiz.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("quiz_question")
public class QuizQuestion {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String quizId;
    private String questionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
