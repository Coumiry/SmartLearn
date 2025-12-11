package com.smartlearn.quiz.exception;

import com.smartlearn.common.response.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class QuizExceptionHandler {

    @ExceptionHandler(QuizException.class)
    public R<?> handleQuizException(QuizException e) {
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public R<?> handleRuntimeException(RuntimeException e) {
        return R.fail(500, e.getMessage());
    }
}