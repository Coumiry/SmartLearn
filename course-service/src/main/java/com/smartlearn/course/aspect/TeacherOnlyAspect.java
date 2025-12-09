package com.smartlearn.course.aspect;

import com.smartlearn.course.annotation.TeacherOnly;
import com.smartlearn.course.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TeacherOnlyAspect {

    private final HttpServletRequest request;

    @Before("@annotation(teacherOnly) || @within(teacherOnly)")
    public void checkTeacherRole(JoinPoint joinPoint, TeacherOnly teacherOnly) {
        // 从网关透传过来的请求头拿角色
        String role = request.getHeader("X-User-Role");

        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            throw new ForbiddenException("当前操作需要教师或管理员权限");
        }
    }
}
