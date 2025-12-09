package com.smartlearn.quiz.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("user_quiz_answer")
public class UserQuizAnswer {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String userId;
    private String quizId;
    private String questionId;
    private String userAnswer;
    private Integer isCorrect;
    private Integer score;
    private LocalDateTime submitTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
