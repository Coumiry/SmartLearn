package com.smartlearn.quiz.exception;

import lombok.Getter;

@Getter
public class QuizException extends RuntimeException {
    private final int code;

    public QuizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public QuizException(String message) {
        this(500, message);
    }
}