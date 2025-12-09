package com.smartlearn.auth.user.controller;

import com.smartlearn.auth.user.dto.LoginRequest;
import com.smartlearn.auth.user.dto.LoginResponse;
import com.smartlearn.auth.user.dto.RegisterRequest;
import com.smartlearn.auth.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    @PostMapping("/register")
    public String register(@RequestBody @Validated RegisterRequest request) {
        userService.register(request);
        return "注册成功";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Validated LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from auth-service";
    }
}
