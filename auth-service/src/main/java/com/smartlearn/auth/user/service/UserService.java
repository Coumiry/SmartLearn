package com.smartlearn.auth.user.service;

import com.smartlearn.auth.user.dto.LoginRequest;
import com.smartlearn.auth.user.dto.LoginResponse;
import com.smartlearn.auth.user.dto.RegisterRequest;

public interface UserService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
