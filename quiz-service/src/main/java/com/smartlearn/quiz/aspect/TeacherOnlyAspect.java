package com.smartlearn.quiz.aspect;

import com.smartlearn.common.annotation.TeacherOnly;
import com.smartlearn.common.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TeacherOnlyAspect {

    private final HttpServletRequest request;

    @Before("@annotation(teacherOnly) || @within(teacherOnly)")
    public void checkTeacherRole(JoinPoint joinPoint, TeacherOnly teacherOnly) {
        // 从网关透传过来的请求头拿角色
        String role = request.getHeader("X-User-Role");

        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            log.warn("Teacher 访问被拒绝，role={}",role);
            throw new ForbiddenException("当前操作需要教师或管理员权限");
        }
    }
}
