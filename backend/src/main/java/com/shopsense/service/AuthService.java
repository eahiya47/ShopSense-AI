package com.shopsense.service;

import com.shopsense.dto.AuthResponse;
import com.shopsense.dto.LoginRequest;
import com.shopsense.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);
}
