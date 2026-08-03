package com.shopsense.service;

import com.shopsense.dto.UserProfileResponse;

public interface UserService {
    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse getUserByEmail(String email);
}
